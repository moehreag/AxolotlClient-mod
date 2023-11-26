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

package io.github.axolotlclient.mixin;

import java.util.Objects;
import java.util.function.Supplier;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.FriendsSidebar;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	private static boolean axolotlclient$hasModMenu() {
		return QuiltLoader.isModLoaded("modmenu") && !QuiltLoader.isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void axolotlclient$addFriendsSidebarButton(CallbackInfo ci) {
		if (API.getInstance().isConnected()) {
			addDrawableChild(ButtonWidget.builder(Text.translatable("api.friends"),
				button -> MinecraftClient.getInstance().setScreen(new FriendsSidebar(this))).positionAndSize(10, height - 30, 75, 20).build());
		}
	}

	@Redirect(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;m_rkfzqxdi(Lnet/minecraft/text/Text;Ljava/lang/String;)Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 1))
	private ButtonWidget axolotlclient$addClientOptionsButton(GameMenuScreen instance, Text text, String string) {
		if (axolotlclient$hasModMenu())
			return m_rkfzqxdi(text, string);

		return m_cqzqwlun(Text.translatable("title_short"), () -> new HudEditScreen(this));
	}

	@Shadow
	protected abstract ButtonWidget m_rkfzqxdi(Text text, String string);

	@Shadow
	protected abstract ButtonWidget m_cqzqwlun(Text text, Supplier<Screen> supplier);

	@ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 1), index = 1)
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
