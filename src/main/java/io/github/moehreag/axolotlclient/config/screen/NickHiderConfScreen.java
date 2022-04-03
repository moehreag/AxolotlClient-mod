package io.github.moehreag.axolotlclient.config.screen;

import io.github.moehreag.axolotlclient.config.widgets.BooleanButtonWidget;
import io.github.moehreag.axolotlclient.config.widgets.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.input.Keyboard;

import static io.github.moehreag.axolotlclient.Axolotlclient.CONFIG;

public class NickHiderConfScreen extends ConfScreen {

    private TextFieldWidget name;

    public NickHiderConfScreen(Screen parent){
        super("nickHiderConf.title", parent);
    }

    @Override
    public void init() {
        super.init();
        Keyboard.enableRepeatEvents(true);
        this.buttons.add(new BooleanButtonWidget(1, this.width / 2 - 155, this.height / 6 + 72 - 6, "hideNames" , CONFIG.hideNames));
        name = new TextFieldWidget(3, this.width / 2 - 155, this.height / 6 + 96 + 10);

        name.write(CONFIG.name.get());

        this.buttons.add(new BooleanButtonWidget(5, this.width / 2 - 155, this.height / 6 + 120 + 16, "hideOwnSkin", CONFIG.hideOwnSkin));
        this.buttons.add(new BooleanButtonWidget(6, this.width / 2 + 5, this.height / 6 + 120 + 16, "hideOtherSkins", CONFIG.hideOtherSkins));

    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        super.render(mouseX, mouseY, tickDelta);

        drawWithShadow(this.textRenderer, I18n.translate("nameDesc"), this.width / 2 - 155, this.height/6 + 96 - 4, 10526880);

        name.render();
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        super.buttonClicked(button);
        if(button.id>0){
            if(button.id==1) CONFIG.hideNames.toggle();
            if(button.id==5) CONFIG.hideOwnSkin.toggle();
            if(button.id==6) CONFIG.hideOtherSkins.toggle();

            MinecraftClient.getInstance().openScreen(this);
        }
    }

    @Override
    public void tick() {
        name.tick();
        super.tick();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        name.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }
}