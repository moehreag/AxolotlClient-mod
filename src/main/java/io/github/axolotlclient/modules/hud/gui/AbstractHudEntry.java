package io.github.axolotlclient.modules.hud.gui;

import io.github.axolotlclient.AxolotlclientConfig.Color;
import io.github.axolotlclient.AxolotlclientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlclientConfig.options.DoubleOption;
import io.github.axolotlclient.AxolotlclientConfig.options.OptionBase;
import io.github.axolotlclient.AxolotlclientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHudEntry extends DrawUtil implements HudEntry {

    protected final BooleanOption enabled = new BooleanOption("axolotlclient.enabled", value -> onBoundsUpdate(), false);
    protected final DoubleOption scale = new DoubleOption("axolotlclient.scale", 1, 0, 5);
    private final DoubleOption x;
    private final DoubleOption y;

    private Rectangle trueBounds = null;
    private Rectangle renderBounds = null;
    private DrawPosition truePosition = null;
    private DrawPosition renderPosition = new DrawPosition(0, 0);

    @Setter
    @Getter
    protected int width;
    @Setter @Getter
    protected int height;

    @Setter
    protected boolean hovered = false;
    protected MinecraftClient client = MinecraftClient.getInstance();

    public AbstractHudEntry(int width, int height) {
        x = new DoubleOption("x", value -> onBoundsUpdate(), getDefaultX(), 0, 1);
        y = new DoubleOption("y", getDefaultY(), 0, 1);
        this.width = width;
        this.height = height;
    }

    public static int floatToInt(float percent, int max, int offset) {
        return MathHelper.clamp(Math.round((max - offset) * percent), 0, max);
    }

    public static float intToFloat(int current, int max, int offset) {
        return MathHelper.clamp((float) (current) / (max - offset), 0, 1);
    }

    public void init() {}

    public void renderPlaceholderBackground(MatrixStack matrices) {
        if (hovered) {
            fillRect(matrices, getTrueBounds(), Color.SELECTOR_BLUE.withAlpha(100));
        } else {
            fillRect(matrices, getTrueBounds(), Color.WHITE.withAlpha(50));
        }
        outlineRect(matrices, getTrueBounds(), Color.BLACK);
    }

    public int getRawX() {
        return getPos().x;
    }

    public void setX(int x) {
        this.x.set((double) intToFloat(x, client.getWindow().getScaledWidth(),
                0
        ));
    }

    public int getRawY() {
        return getPos().y();
    }

    public void setY(int y) {
        this.y.set((double) intToFloat(y, client.getWindow().getScaledHeight(),
                0
        ));
    }

    public Rectangle getTrueBounds() {
        return trueBounds;
    }

    /**
     * Gets the hud's bounds when the matrix has already been scaled.
     *
     * @return The bounds.
     */
    public Rectangle getBounds() {
        return renderBounds;
    }

    @Override
    public float getScale() {
        return scale.get().floatValue();
    }

    public void scale(MatrixStack matrices) {
        float scale = getScale();
        matrices.scale(scale, scale, 1);
    }

    @Override
    public DrawPosition getPos() {
        return renderPosition;
    }

    @Override
    public DrawPosition getTruePos() {
        return truePosition;
    }

    @Override
    public void onBoundsUpdate() {
        setBounds();
    }

    public void setBounds() {
        setBounds(getScale());
    }

    @Override
    public int getRawTrueX() {
        return truePosition.x();
    }

    @Override
    public int getRawTrueY() {
        return truePosition.y();
    }

    @Override
    public int getTrueWidth() {
        if (trueBounds == null) {
            return HudEntry.super.getTrueWidth();
        }
        return trueBounds.width();
    }

    @Override
    public int getTrueHeight() {
        if (trueBounds == null) {
            return HudEntry.super.getTrueHeight();
        }
        return trueBounds.height();
    }

    public void setBounds(float scale) {
        if (client.getWindow() == null) {
            truePosition = new DrawPosition(0, 0);
            renderPosition = new DrawPosition(0, 0);
            renderBounds = new Rectangle(0, 0, 1, 1);
            trueBounds = new Rectangle(0, 0, 1, 1);
            return;
        }
        int scaledX = floatToInt(x.get().floatValue(), client.getWindow().getScaledWidth(), 0) - offsetTrueWidth();
        int scaledY = floatToInt(y.get().floatValue(), client.getWindow().getScaledHeight(), 0) - offsetTrueHeight();
        if (scaledX < 0) {
            scaledX = 0;
        }
        if (scaledY < 0) {
            scaledY = 0;
        }
        int trueWidth = (int) (getWidth() * getScale());
        if (trueWidth < client.getWindow().getScaledWidth() && scaledX + trueWidth > client.getWindow().getScaledWidth()) {
            scaledX = client.getWindow().getScaledWidth() - trueWidth;
        }
        int trueHeight = (int) (getHeight() * getScale());
        if (trueHeight < client.getWindow().getScaledHeight() && scaledY + trueHeight > client.getWindow().getScaledHeight()) {
            scaledY = client.getWindow().getScaledHeight() - trueHeight;
        }
        truePosition = new DrawPosition(scaledX, scaledY);
        renderPosition = truePosition.divide(getScale());
        renderBounds = new Rectangle(renderPosition.x(), renderPosition.y(), getWidth(), getHeight());
        trueBounds = new Rectangle(scaledX, scaledY, (int) (getWidth() * getScale()), (int) (getHeight() * getScale()));
    }

    /*@Override
    public Tab toTab() {
        // Need to cast KronConfig to Option
        return Tab.ofOptions(getId(), getNameKey(), getConfigurationOptions().stream().map((o -> (Option<?>) o)).collect(Collectors.toList()));
    }*/

    /**
     * Returns a list of options that should be shown in configuration screens
     *
     * @return List of options
     */
    @Override
    public List<OptionBase<?>> getConfigurationOptions() {
        List<OptionBase<?>> options = new ArrayList<>();
        options.add(enabled);
        options.add(scale);
        return options;
    }

    /**
     * Returns a list of options that should be saved. By default, this includes {@link #getConfigurationOptions()}
     *
     * @return
     */
    @Override
    public List<OptionBase<?>> getSaveOptions() {
        List<OptionBase<?>> options = getConfigurationOptions();
        options.add(x);
        options.add(y);
        return options;
    }

    public OptionCategory getOptionsAsCategory(){
        OptionCategory cat = new OptionCategory(getNameKey(), false);
        cat.add(getConfigurationOptions());
        return cat;
    }
    public OptionCategory getAllOptions() {
        List<OptionBase<?>> options = getSaveOptions();
        OptionCategory cat = new OptionCategory(getNameKey());
        cat.add(options);
        return cat;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public void setEnabled(boolean value) {
        enabled.set(value);
    }

}
