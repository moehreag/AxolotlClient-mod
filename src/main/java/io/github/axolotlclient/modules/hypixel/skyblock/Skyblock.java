/*
 * Copyright © 2021-2022 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.skyblock;

import io.github.axolotlclient.AxolotlclientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlclientConfig.options.KeyBindOption;
import io.github.axolotlclient.AxolotlclientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import lombok.Getter;
import net.minecraft.client.options.KeyBinding;

public class Skyblock implements AbstractHypixelMod {

    @Getter
    private final static Skyblock Instance = new Skyblock();

    private final OptionCategory category = new OptionCategory("axolotlclient.skyblock");

    public final BooleanOption rotationLocked = new BooleanOption("axolotlclient.rotationLocked", false);
    private final KeyBindOption lock = new KeyBindOption("axolotlclient.lockRotation",
            new KeyBinding("axolotlclient.lockRotation", 25, "axolotlclient.category.axolotlclient"),
            keyBinding -> rotationLocked.toggle());

    @Override
    public void init() {
        category.add(rotationLocked, lock);
    }

    @Override
    public OptionCategory getCategory() {
        return category;
    }
}