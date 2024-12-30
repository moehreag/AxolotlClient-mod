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

package io.github.axolotlclient.util.options.vanilla;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.OptionCategoryImpl;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.BooleanWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.CategoryWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;

public class QuickToggleCategoryWidget extends CategoryWidget {
	private BooleanWidget enabledButton;

	public QuickToggleCategoryWidget(int x, int y, int width, int height, OptionCategoryImpl category) {
		super(x, y, width, height, category);
		category.getOptions().stream()
			.filter(o -> o instanceof BooleanOption)
			.map(o -> (BooleanOption) o)
			.filter(o -> "enabled".equals(o.getName())).findFirst()
			.ifPresent(booleanOption -> enabledButton = new BooleanWidget(x + (width - 33), y + 3, 30, height - 5, booleanOption));
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {

		if (enabledButton != null && enabledButton.isMouseOver(mouseX, mouseY)) {
			this.hovered = false;
			return true;
		}
		return super.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void drawWidget(int mouseX, int mouseY, float delta) {
		super.drawWidget(mouseX, mouseY, delta);

		if (enabledButton != null) {
			enabledButton.setY(getY() + 2);
			enabledButton.update();
			enabledButton.render(mouseX, mouseY, delta);
		}
	}

	@Override
	protected void drawScrollingText(TextRenderer textRenderer, int i, Color j) {
		int k = this.getX() + i;
		int l = this.getX() + this.getWidth() - i;
		int center = getX() + (getWidth() / 2);
		if (enabledButton != null) {
			l -= enabledButton.getWidth() + 4;
			center -= enabledButton.getWidth() / 2 + 2;
		}
		drawScrollingText(textRenderer, this.getMessage(), center, k, this.getY(), l, this.getY() + this.getHeight(), j);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {

		if (enabledButton != null &&
			enabledButton.isHovered()) {
			playDownSound(Minecraft.getInstance().getSoundManager());
			enabledButton.onPress();
			return true;
		}
		return this.hovered && super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.active || !this.visible) {
			return false;
		} else if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
			return false;
		} else {
			this.playDownSound(Minecraft.getInstance().getSoundManager());
			mouseClicked(0, 0, 0);
			return true;
		}
	}
}