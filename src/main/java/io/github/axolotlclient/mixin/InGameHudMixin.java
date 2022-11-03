package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.PotionsHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ActionBarHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.CrosshairHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.HotbarHUD;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ScoreboardHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow private int scaledHeight;

	@Shadow private int scaledWidth;

	@Shadow private @Nullable Text overlayMessage;

	@Shadow private int overlayRemaining;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/util/math/MatrixStack;)V"))
	private void onHudRender(MatrixStack matrices, float tickDelta, CallbackInfo ci){
		if(!MinecraftClient.getInstance().options.hudHidden) {
			HudManager.getInstance().render(matrices, tickDelta);
		}
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void renderStatusEffect(MatrixStack matrices, CallbackInfo ci) {
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud != null && hud.isEnabled()) {
			if(MinecraftClient.getInstance().options.debugEnabled && !hud.overridesF3()){
				return;
			}
			ci.cancel();
		}
	}

	@Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	public void renderScoreboard(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;overlayMessage:Lnet/minecraft/text/Text;", ordinal = 0))
	public void clearActionBar(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (overlayMessage == null || overlayRemaining<=0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0))
	public int getActionBar(TextRenderer instance, MatrixStack matrices, Text message, float x, float y, int color){
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()){
			hud.setActionBar(message, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return instance.draw(matrices, message, x, y, color);
		}
	}

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void customHotbar(float tickDelta, MatrixStack matrices, CallbackInfo ci){
        HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
        if(hud.isEnabled()){
            ci.cancel();
        }
    }

    @ModifyArgs(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    public void setItemNamePos(Args args){
        HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
        if(hud.isEnabled()){
            args.set(2, ((Integer) hud.getX()).floatValue() + ((hud.getWidth() * hud.getScale())-
                MinecraftClient.getInstance().textRenderer.getWidth((StringVisitable) args.get(1)))/2);
            args.set(3, ((Integer) hud.getY()).floatValue() - 36 +
                (!MinecraftClient.getInstance().interactionManager.hasStatusBars() ? 14 : 0));
        }
    }

	@ModifyArgs(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
	public void moveHorseHealth(Args args){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			args.set(1, hud.getX());
			args.set(2, hud.getY() - 7);
		}
	}

	@ModifyArgs(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
	public void moveXPBar(Args args){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			args.set(1, hud.getX());
			args.set(2, hud.getY()-7);
		}
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I"))
	public int moveXPBarHeight(InGameHud instance){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			return hud.getY()+22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I"))
	public int moveXPBarWidth(InGameHud instance){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			return hud.getX()*2+ hud.getWidth();
		}
		return scaledWidth;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I"))
	public int moveStatusBarsHeight(InGameHud instance){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			return hud.getY()+22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I"))
	public int moveStatusBarsWidth(InGameHud instance){
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if(hud.isEnabled()){
			return hud.getX()*2+ hud.getWidth();
		}
		return scaledWidth;
	}
}
