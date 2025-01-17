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

package io.github.axolotlclient.mixin;

import java.util.Objects;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.ChatsSidebar;
import io.github.axolotlclient.api.FriendsScreen;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	private static boolean axolotlclient$hasModMenu() {
		return FabricLoader.getInstance().isModLoaded("modmenu") && !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void axolotlclient$sidebarButton(CallbackInfo ci) {
		if (API.getInstance().isAuthenticated()) {
			int buttonY = height - (axolotlclient$hasModMenu() ? 50 : 30);
			if (APIOptions.getInstance().addShortcutButtons.get()) {
				addButton(new ButtonWidget(10, buttonY, 75, 20, new TranslatableText("api.friends"),
					buttonWidget -> client.openScreen(new FriendsScreen(this))));
				buttonY -= 25;
			}
			addButton(new ButtonWidget(10, buttonY, 75, 20, new TranslatableText("api.chats"),
				buttonWidget -> client.openScreen(new ChatsSidebar(this))));
		}
	}

	@ModifyArgs(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V", ordinal = 3))
	public void addClientOptionsButton(Args args) {
		if (axolotlclient$hasModMenu())
			return;

		args.set(4, new TranslatableText("title_short"));
		args.set(5, (ButtonWidget.PressAction) (buttonWidget) -> MinecraftClient.getInstance()
			.openScreen(new HudEditScreen(((GameMenuScreen) (Object) this))));
	}

	@ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V", ordinal = 1), index = 5)
	private ButtonWidget.PressAction axolotlclient$clearFeatureRestrictions(ButtonWidget.PressAction onPress) {
		return (buttonWidget) -> {
			if (Objects.equals(HypixelMods.getInstance().cacheMode.get(),
				HypixelMods.HypixelCacheMode.ON_CLIENT_DISCONNECT.toString())) {
				HypixelAbstractionLayer.clearPlayerData();
			}
			onPress.onPress(buttonWidget);
		};
	}
}
