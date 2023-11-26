package io.github.axolotlclient.modules.auth;

import java.net.URI;
import java.util.List;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class DeviceCodeDisplayScreen extends Screen {
	private final Screen parent;
	private final String verificationUri, userCode;
	private final List<OrderedText> message;
	private int ticksLeft;
	private Text status;
	private boolean working;

	public DeviceCodeDisplayScreen(Screen parent, DeviceFlowData data) {
		super(Text.translatable("auth.add"));
		this.parent = parent;
		this.message = MinecraftClient.getInstance().textRenderer.wrapLines(Text.of(data.getMessage()), 400);
		this.verificationUri = data.getVerificationUri();
		this.userCode = data.getUserCode();
		this.ticksLeft = data.getExpiresIn() * 20;
		this.status = Text.translatable("auth.time_left",
			((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s");
		data.setStatusConsumer(s -> {
			if (s.equals("auth.finished")) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(parent));
			}
			working = true;
			clearChildren();
			status = Text.translatable(s);
		});
	}

	@Override
	protected void init() {
		addDrawableChild(ButtonWidget.builder(Text.translatable("auth.copy_and_open"),
			buttonWidget -> {
				client.keyboard.setClipboard(userCode);
				OSUtil.getOS().open(URI.create(verificationUri), AxolotlClient.LOGGER);
			}).positionAndSize(width / 2 - 100, height / 2, 200, 20).build());
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);

		drawCenteredText(graphics, client.textRenderer, title, width/2, 25, -1);

		int y = height / 4;
		for (OrderedText orderedText : message) {
			client.textRenderer.drawWithShadow(graphics, orderedText, width / 2f - client.textRenderer.getWidth(orderedText) / 2f, y, -1);
			y += 10;
		}
		drawCenteredText(graphics, client.textRenderer, working ? status : Text.translatable("auth.time_left",
				((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s"),
			width / 2, y + 10, -1);
	}

	@Override
	public void tick() {
		ticksLeft--;
	}
}
