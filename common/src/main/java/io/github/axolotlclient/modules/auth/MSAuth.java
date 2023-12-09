/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

// Partly oriented on In-Game-Account-Switcher by The-Fireplace, VidTu
public class MSAuth {

	private static final String CLIENT_ID = "938592fc-8e01-4c6d-b56d-428c7d9cf5ea"; // AxolotlClient MSA ClientID
	private static final String SCOPES = "XboxLive.signin offline_access";

	private final Supplier<String> languageSupplier;
	private final Logger logger;
	private final Accounts accounts;

	public MSAuth(Logger logger, Accounts accounts, Supplier<String> languageSupplier) {
		this.logger = logger;
		this.accounts = accounts;
		this.languageSupplier = languageSupplier;
	}

	public void startDeviceAuth(Runnable whenFinished) {
		try {
			String[] lang = languageSupplier.get().replace("_", "-").split("-");
			logger.debug("starting ms device auth flow");
			// https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-device-code#device-authorization-response
			RequestBuilder builder = RequestBuilder.post()
				.setEntity(new UrlEncodedFormEntity(ImmutableList.of(
					new BasicNameValuePair("client_id", CLIENT_ID),
					new BasicNameValuePair("scope", SCOPES)), StandardCharsets.UTF_8))
				.addHeader("ContentType", "application/x-www-form-urlencoded")
				.setUri("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode?mkt=" + lang[0] + "-" + lang[1].toUpperCase(Locale.ROOT));
			JsonObject object = NetworkUtil.request(builder.build(), getHttpClient()).getAsJsonObject();
			int expiresIn = object.get("expires_in").getAsInt();
			String deviceCode = object.get("device_code").getAsString();
			String userCode = object.get("user_code").getAsString();
			String verificationUri = object.get("verification_uri").getAsString();
			int interval = object.get("interval").getAsInt();
			String message = object.get("message").getAsString();
			logger.debug("displaying device code to user");
			DeviceFlowData data = new DeviceFlowData(message, verificationUri, deviceCode, userCode, expiresIn, interval);
			accounts.displayDeviceCode(data);

			ThreadExecuter.scheduleTask(() -> {
				logger.debug("waiting for user authorization...");
				long start = System.currentTimeMillis();
				try {
					while (System.currentTimeMillis() < expiresIn * 1000L + start) {
						if ((System.currentTimeMillis() - start) % interval == 0) {
							List<NameValuePair> form = new ArrayList<>();
							form.add(new BasicNameValuePair("client_id", CLIENT_ID));
							form.add(new BasicNameValuePair("device_code", deviceCode));
							form.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:device_code"));
							RequestBuilder requestBuilder = RequestBuilder.post()
								.setUri("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
								.addHeader("ContentType", "application/x-www-form-urlencoded")
								.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
							JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();

							if (response.has("refresh_token") && response.has("access_token")) {
								data.setStatus("auth.working");
								authenticateFromMSTokens(new AbstractMap.SimpleImmutableEntry<>(response.get("access_token").getAsString(),
									response.get("refresh_token").getAsString()), true, whenFinished);
								data.setStatus("auth.finished");
								break;
							}

							if (response.has("error")) {
								String error = response.get("error").getAsString();
								switch (error) {
									case "authorization_pending":
										continue;
									case "bad_verification_code":
										throw new IllegalStateException("Bad verification code! " + response);
									case "authorization_declined":
									case "expired_token":
									default:
										break;
								}
							}
						}
					}
				} catch (Exception e) {
					logger.error("Error while waiting for user authentication: ", e);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void authenticateFromMSTokens(Map.Entry<String, String> msTokens, boolean login, Runnable whenFinished) {
		try {
			logger.debug("getting xbl token... ");
			String xblToken = authXbl(msTokens.getKey());
			logger.debug("getting xsts token... ");
			Map.Entry<String, String> xsts = authXstsMC(xblToken);
			logger.debug("getting mc auth token...");
			String accessToken = authMC(xsts.getValue(), xsts.getKey());
			if (checkOwnership(accessToken)) {
				logger.debug("finished auth flow!");
				Account account = new Account(getMCProfile(accessToken), accessToken, msTokens.getKey(), msTokens.getValue());
				if (accounts.isContained(account.getUuid())) {
					accounts.getAccounts().removeAll(accounts.getAccounts().stream().filter(acc -> acc.getUuid().equals(account.getUuid())).collect(Collectors.toList()));
				}
				accounts.addAccount(account);
				if (login) {
					accounts.login(account);
				}
				whenFinished.run();
			} else {
				throw new IllegalStateException("Do you actually own the game?");
			}
		} catch (Exception e) {
			logger.error("Failed to authenticate!", e);
		}
	}

	public String authXbl(String code) throws IOException {
		JsonObject object = new JsonObject();
		JsonObject properties = new JsonObject();
		properties.add("AuthMethod", new JsonPrimitive("RPS"));
		properties.add("SiteName", new JsonPrimitive("user.auth.xboxlive.com"));
		properties.add("RpsTicket", new JsonPrimitive("d=" + code));
		object.add("Properties", properties);
		object.add("RelyingParty", new JsonPrimitive("http://auth.xboxlive.com"));
		object.add("TokenType", new JsonPrimitive("JWT"));
		RequestBuilder requestBuilder = RequestBuilder.post()
			.setUri("https://user.auth.xboxlive.com/user/authenticate")
			.setEntity(new StringEntity(object.toString(), ContentType.APPLICATION_JSON))
			.addHeader("Content-Type", "application/json")
			.addHeader("Accept", "application/json");

		JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();
		return response.get("Token").getAsString();
	}

	public Map.Entry<String, String> authXstsMC(String xblToken) throws IOException {
		String body = "{" +
			"    \"Properties\": {" +
			"        \"SandboxId\": \"RETAIL\"," +
			"        \"UserTokens\": [" +
			"            \"" + xblToken + "\"" +
			"        ]" +
			"    }," +
			"    \"RelyingParty\": \"rp://api.minecraftservices.com/\"," +
			"    \"TokenType\": \"JWT\"" +
			" }";
		JsonObject response = NetworkUtil.postRequest("https://xsts.auth.xboxlive.com/xsts/authorize", body, getHttpClient(), true).getAsJsonObject();
		return new AbstractMap.SimpleImmutableEntry<>(response.get("Token").getAsString(), response.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString());
	}

	public String authMC(String userhash, String xsts) throws IOException {
		return NetworkUtil.postRequest("https://api.minecraftservices.com/authentication/login_with_xbox",
			"{\"identityToken\": \"XBL3.0 x=" + userhash + ";" + xsts + "\"\n}",
			getHttpClient(), true).getAsJsonObject().get("access_token").getAsString();
	}

	public boolean checkOwnership(String accessToken) throws IOException {
		JsonObject response = NetworkUtil.request(RequestBuilder.get()
			.setUri("https://api.minecraftservices.com/entitlements/mcstore")
			.addHeader("Authorization", "Bearer " + accessToken).build(), getHttpClient(), true).getAsJsonObject();

		return response.get("items").getAsJsonArray().size() != 0;
	}

	public JsonObject getMCProfile(String accessToken) throws IOException {
		return NetworkUtil.request(RequestBuilder.get()
			.setUri("https://api.minecraftservices.com/minecraft/profile")
			.addHeader("Authorization", "Bearer " + accessToken).build(), getHttpClient(), true).getAsJsonObject();
	}

	private CloseableHttpClient getHttpClient() {
		return NetworkUtil.createHttpClient("Auth");
	}

	public void refreshToken(String token, Account account, Runnable runAfter) {
		try {
			logger.debug("refreshing auth code... ");
			List<NameValuePair> form = new ArrayList<>();
			form.add(new BasicNameValuePair("client_id", CLIENT_ID));
			form.add(new BasicNameValuePair("refresh_token", token));
			form.add(new BasicNameValuePair("scope", SCOPES));
			form.add(new BasicNameValuePair("grant_type", "refresh_token"));
			RequestBuilder requestBuilder = RequestBuilder.post()
				.setUri("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.setEntity(new UrlEncodedFormEntity(form))
				.addHeader("Accept", "application/json");

			JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();

			if (response.has("error_codes")) {
				if (response.get("error_codes").getAsJsonArray().get(0).getAsInt() == 70000) {
					accounts.showAccountsExpiredScreen(account);
				}
				return;
			}

			authenticateFromMSTokens(new AbstractMap.SimpleImmutableEntry<>(response.get("access_token").getAsString(),
				response.get("refresh_token").getAsString()), false, runAfter);

		} catch (Exception e) {
			logger.error("Failed to refresh Auth token! ", e);
		}
	}
}
