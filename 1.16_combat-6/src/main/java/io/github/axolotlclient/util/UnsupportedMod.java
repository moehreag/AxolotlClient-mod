/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

public class UnsupportedMod {

	private final String name;

	private final UnsupportedReason[] reason;

	public UnsupportedMod(String name, io.github.axolotlclient.util.UnsupportedMod.UnsupportedReason... reason) {
		this.name = name;
		this.reason = reason;
	}

	public String name() {
		return name;
	}

	public UnsupportedReason[] reason() {
		return reason;
	}

	public enum UnsupportedReason {

		BAN_REASON("be bannable on lots of servers"), CRASH("crash your game"),
		MIGHT_CRASH("have effects that could crash your game"),
		UNKNOWN_CONSEQUENSES("have unknown consequences in combination with this mod");

		private final String description;

		UnsupportedReason(String desc) {
			description = desc;
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
