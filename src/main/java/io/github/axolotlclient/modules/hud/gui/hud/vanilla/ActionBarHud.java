package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import io.github.axolotlclient.AxolotlclientConfig.Color;
import io.github.axolotlclient.AxolotlclientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlclientConfig.options.IntegerOption;
import io.github.axolotlclient.AxolotlclientConfig.options.OptionBase;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ActionBarHud extends TextHudEntry {

    public static final Identifier ID = new Identifier("kronhud", "actionbarhud");

    public IntegerOption timeShown = new IntegerOption("timeshown", ID.getPath(), 60, 40, 300);
    public BooleanOption customTextColor = new BooleanOption("customtextcolor", ID.getPath(), false);

    @Getter
    private Text actionBar;
    private int ticksShown;
    private int color;
    private final String placeholder = "Action Bar";

    public ActionBarHud() {
        super(115, 13, false);
    }

    public void setActionBar(Text bar, int color) {
        this.actionBar = bar;
        this.color = color;
    }

    @Override
    public void renderComponent(MatrixStack matrices, float delta) {
        if (ticksShown >= timeShown.get()) {
            this.actionBar = null;
        }
        Color vanillaColor = new Color(color);
        if (this.actionBar != null) {

            if (shadow.get()) {
                client.textRenderer.drawWithShadow(matrices, actionBar,
                        (float) getPos().x() + Math.round((float) getWidth() / 2) - (float) client.textRenderer.getWidth(actionBar) / 2,
                        (float) getPos().y() + 3,
                        customTextColor.get() ? (
                                textColor.get().getAlpha() == 255 ?
                                new Color(
                                        textColor.get().getRed(),
                                        textColor.get().getGreen(),
                                        textColor.get().getBlue(),
                                        vanillaColor.getAlpha()
                                ).getAsInt() :
                                textColor.get().getAsInt()
                        ) :
                        color
                );
            } else {

                client.textRenderer.draw(matrices, actionBar,
                        (float) getPos().x() + Math.round((float) getWidth() / 2) - ((float) client.textRenderer.getWidth(actionBar) / 2),
                        (float) getPos().y() + 3,
                        customTextColor.get() ? (
                                textColor.get().getAlpha() == 255 ?
                                new Color(
                                        textColor.get().getRed(),
                                        textColor.get().getGreen(),
                                        textColor.get().getBlue(),
                                        vanillaColor.getAlpha()
                                ).getAsInt() :
                                textColor.get().getAsInt()
                        ) :
                        color
                );
            }
            ticksShown++;
        } else {
            ticksShown = 0;
        }
    }

    @Override
    public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
        client.textRenderer.draw(
                matrices, placeholder,
                (float) getPos().x() + Math.round((float) getWidth() / 2) - (float) client.textRenderer.getWidth(placeholder) / 2,
                (float) getPos().y() + 3, -1
        );
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public List<OptionBase<?>> getConfigurationOptions() {
        List<OptionBase<?>> options = super.getConfigurationOptions();
        options.add(shadow);
        options.add(timeShown);
        options.add(customTextColor);
        options.add(textColor);
        return options;
    }
}
