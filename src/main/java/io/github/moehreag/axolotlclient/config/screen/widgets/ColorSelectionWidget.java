package io.github.moehreag.axolotlclient.config.screen.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.moehreag.axolotlclient.config.Color;
import io.github.moehreag.axolotlclient.config.options.ColorOption;
import io.github.moehreag.axolotlclient.modules.hud.util.DrawUtil;
import io.github.moehreag.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ColorSelectionWidget extends ButtonWidget {
    private final ColorOption option;

    protected Rectangle pickerImage;
    //private final Rectangle rect;

    public ColorSelectionWidget(ColorOption option) {
        super(0, 100, 50, "");
        this.option=option;
        Window window= new Window(MinecraftClient.getInstance());
        width=window.getWidth()-200;
        height=window.getHeight()-100;

        pickerImage = new Rectangle(120, 70, width/2, height/3);

        //rect = new Rectangle(100, 50, width-200, height-100);
    }

    @Override
    public void render(MinecraftClient client, int mouseX, int mouseY) {

        DrawUtil.fillRect(new Rectangle(100, 50, width, height), Color.DARK_GRAY.withAlpha(127));
        DrawUtil.outlineRect(new Rectangle(100, 50, width, height), Color.BLACK);

        DrawUtil.outlineRect(pickerImage, Color.DARK_GRAY.withAlpha(127));

        GlStateManager.color3f(1, 1, 1);
        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("axolotlclient", "textures/gui/colorwheel.png"));
        DrawableHelper.drawTexture(pickerImage.x, pickerImage.y, 0, 0, pickerImage.width, pickerImage.height, pickerImage.width, pickerImage.height);

        //super.render(client, mouseX, mouseY);
    }

    public void onClick(int mouseX, int mouseY){
        if(pickerImage.isMouseOver(mouseX, mouseY)) {
            ByteBuffer buf = ByteBuffer.allocateDirect(4);
            IntBuffer color = buf.asIntBuffer();

            MinecraftClient.getInstance().getFramebuffer().bind(true);
            GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);

            System.out.println(buf.get());

            option.set(new Color(buf.get(0) & 0xFF, buf.get(1) & 0xFF, buf.get(2) & 0xFF, buf.get(3) & 0xFF));
        }
    }


}
