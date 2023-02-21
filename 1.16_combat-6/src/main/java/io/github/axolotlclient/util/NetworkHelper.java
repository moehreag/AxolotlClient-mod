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

package io.github.axolotlclient.util;

import com.google.gson.JsonElement;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class NetworkHelper {

	// In case we ever implement more of HyCord's features...
	public static JsonElement getRequest(String site) {
		return getRequest(site, HttpClients.custom().disableAutomaticRetries().build());
	}

	public static JsonElement getRequest(String url, CloseableHttpClient client) {
		try {
			return NetworkUtil.getRequest(url, client);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    /*public static String getUuid(String username) {
        JsonElement response = getRequest("https://api.mojang.com/users/profiles/minecraft/" + username);
        if (response == null)
            return null;
        return response.getAsJsonObject().get("id").getAsString();
    }

    public static BufferedImage getImage(String imgUrl) {
        try (CloseableHttpClient client = HttpClients.custom().disableAutomaticRetries().build()) {

            HttpGet get = new HttpGet(imgUrl);
            HttpResponse response = client.execute(get);

            client.close();
            return ImageIO.read(response.getEntity().getContent());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
