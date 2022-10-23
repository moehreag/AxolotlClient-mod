package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.BossBarHud.class)
public abstract class BossBarHudMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(MatrixStack matrices, CallbackInfo ci) {
		BossBarHud hud = (BossBarHud) HudManager.getInstance().get(BossBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}
}
