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

package io.github.axolotlclient.modules.screenshotUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.AllArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class ScreenshotUtils extends AbstractModule {

	private static final ScreenshotUtils Instance = new ScreenshotUtils();
	private final OptionCategory category = OptionCategory.create("screenshotUtils");
	private final BooleanOption enabled = new BooleanOption("enabled", false);
	private final List<Action> actions = Util.make(() -> {
		List<Action> actions = new ArrayList<>();
		actions.add(new Action("copyAction", Formatting.AQUA,
			"copy_image",
			new CustomClickEvent(ScreenshotCopying::copy)));

		actions.add(new Action("deleteAction", Formatting.LIGHT_PURPLE,
			"delete_image",
			new CustomClickEvent((file) -> {
				try {
					Files.delete(file.toPath());
					io.github.axolotlclient.util.Util.sendChatMessage(
						Text.literal(I18n.translate("screenshot_deleted").replace("<name>", file.getName())));
				} catch (Exception e) {
					AxolotlClient.LOGGER.warn("Couldn't delete Screenshot " + file.getName());
				}
			})));

		actions.add(new Action("openAction", Formatting.WHITE,
			"open_image",
			new CustomClickEvent((file) -> Util.getOperatingSystem().open(file.toURI()))));

		actions.add(new Action("uploadAction", Formatting.LIGHT_PURPLE,
			"upload_image",
			new CustomClickEvent(file -> {
				new Thread("Image Uploader") {
					@Override
					public void run() {
						ImageShare.getInstance().uploadImage(file);
					}
				}.start();
			})));

		// If you have further ideas to what actions could be added here, please let us know!

		return actions;
	});

	private final StringArrayOption autoExec = new StringArrayOption("autoExec", Util.make(() -> {
		List<String> names = new ArrayList<>();
		names.add("off");
		actions.forEach(action -> names.add(action.getName()));
		return names.toArray(new String[0]);
	}), "off");

	public static ScreenshotUtils getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled, autoExec, new GenericOption("imageViewer", "openViewer", () -> {
			MinecraftClient.getInstance().setScreen(new ImageViewerScreen(MinecraftClient.getInstance().currentScreen));
		}));

		AxolotlClient.CONFIG.general.add(category);
	}

	public Text onScreenshotTaken(MutableText text, File shot) {
		if (enabled.get()) {
			Text t = getUtilsText(shot);
			if (t != null) {
				return text.append("\n").append(t);
			}
		}
		return text;
	}

	private @Nullable Text getUtilsText(File file) {
		if (!autoExec.get().equals("off")) {
			actions.parallelStream().filter(action -> autoExec.get().equals(action.getName())).toList()
				.get(0).clickEvent.setFile(file).doAction();
			return null;
		}

		MutableText message = Text.empty().copy();
		actions.parallelStream().map(action -> action.getText(file)).iterator().forEachRemaining(text -> {
			message.append(text);
			message.append(" ");
		});
		return message;
	}

	public interface OnActionCall {

		void doAction(File file);
	}

	@AllArgsConstructor
	public static class Action {

		private final String translationKey;
		private final Formatting formatting;
		private final String hoverTextKey;
		private final CustomClickEvent clickEvent;

		public Text getText(File file) {
			return Text.translatable(translationKey).setStyle(Style.EMPTY.withFormatting(formatting)
				.withClickEvent(clickEvent.setFile(file)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(hoverTextKey))));
		}

		public String getName() {
			return translationKey;
		}
	}

	public static class CustomClickEvent extends ClickEvent {

		private final OnActionCall action;
		private File file;

		public CustomClickEvent(OnActionCall action) {
			super(Action.OPEN_FILE, "");
			this.action = action;
		}

		public void doAction() {
			if (file != null) {
				action.doAction(file);
			} else {
				AxolotlClient.LOGGER.warn("How'd you manage to do this? "
										  + "Now there's a screenshot ClickEvent without a File attached to it!");
			}
		}


		public CustomClickEvent setFile(File file) {
			this.file = file;
			return this;
		}
	}
}