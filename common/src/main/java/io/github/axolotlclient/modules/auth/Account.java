/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonObject;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.AccessLevel;
import lombok.Getter;

public class Account {

	public static final String OFFLINE_TOKEN = "AxolotlClient/Offline";

	@Getter
	private final String uuid;
	@Getter
	private final String name;

	@Getter(AccessLevel.PACKAGE)
	private final String msaToken;

	@Getter
	private final String authToken;
	@Getter(AccessLevel.PACKAGE)
	private final String refreshToken;
	private final Instant expiration;

	public Account(String name, String uuid, String accessToken, Instant expiration, String refreshToken, String msaToken) {
		this.name = name;
		this.uuid = uuid;
		this.authToken = accessToken;
		this.expiration = expiration;
		this.refreshToken = refreshToken;
		this.msaToken = msaToken;
	}

	public Account(String name, String uuid, String accessToken) {
		this(name, uuid.replace("-", ""), accessToken, Instant.EPOCH, "", "");
	}

	public Account(JsonObject profile, String authToken, String msaToken, String refreshToken) {
		this(profile.get("name").getAsString(), profile.get("id").getAsString(), authToken, Instant.now().plus(1, ChronoUnit.DAYS), refreshToken, msaToken);
	}

	private Account(String uuid, String name, String authToken, String msaToken, String refreshToken, long expiration) {
		this(name, uuid, authToken, Instant.ofEpochSecond(expiration), refreshToken, msaToken);
	}

	public static Account deserialize(JsonObject object) {
		String uuid = object.get("uuid").getAsString();
		String name = object.get("name").getAsString();
		String authToken = object.get("authToken").getAsString();
		String refreshToken = object.get("refreshToken").getAsString();
		String msaToken = object.has("msToken") ? object.get("msToken").getAsString() : "";
		long expiration = object.get("expiration").getAsLong();
		return new Account(uuid, name, authToken, msaToken, refreshToken, expiration);
	}

	public void refresh(MSAuth auth, Runnable runAfter) {
		ThreadExecuter.scheduleTask(() -> auth.refreshToken(refreshToken, this, runAfter));
	}

	public boolean isOffline() {
		return authToken.equals(OFFLINE_TOKEN) || authToken.length() < 400;
	}

	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.addProperty("uuid", uuid);
		object.addProperty("name", name);
		object.addProperty("authToken", authToken);
		object.addProperty("msToken", msaToken);
		object.addProperty("refreshToken", refreshToken == null ? "" : refreshToken);
		object.addProperty("expiration", expiration == null ? 0 : expiration.getEpochSecond());
		return object;
	}

	public boolean isExpired() {
		return expiration.isBefore(Instant.now());
	}

	public boolean needsRefresh() {
		return Instant.now().isAfter(expiration.minus(6, ChronoUnit.HOURS));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Account other) {
			return name.equals(other.name) &&
				uuid.equals(other.uuid);
		}
		return false;
	}
}
