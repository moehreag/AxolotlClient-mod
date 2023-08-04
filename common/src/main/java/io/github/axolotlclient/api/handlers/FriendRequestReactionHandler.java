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

package io.github.axolotlclient.api.handlers;

import java.nio.charset.StandardCharsets;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.util.RequestHandler;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.netty.buffer.ByteBuf;

public class FriendRequestReactionHandler implements RequestHandler {
	@Override
	public boolean isApplicable(int packetType) {
		return packetType == Request.Type.FRIEND_REQUEST_REACTION.getType();
	}

	@Override
	public void handle(ByteBuf object, APIError error) {
		byte[] uuid = new byte[16];
		object.getBytes(0x09, uuid);
		String fromUUID = new String(uuid, StandardCharsets.UTF_8);

		if (object.getBoolean(0x19)) {
			API.getInstance().getNotificationProvider().addStatus("api.friends", "api.friends.request.accepted", UUIDHelper.getUsername(fromUUID));
		} else {
			API.getInstance().getNotificationProvider().addStatus("api.friends", "api.friends.request.declined", UUIDHelper.getUsername(fromUUID));
		}
	}
}
