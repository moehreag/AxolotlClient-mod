package io.github.axolotlclient.modules.hud.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.config.options.*;
import io.github.axolotlclient.mixin.AccessorChatHud;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Texts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ChatHud extends AbstractHudEntry {

    public static Identifier ID = new Identifier("axolotlclient", "chathud");
    public BooleanOption background = new BooleanOption("background", "chathud", true);
    public ColorOption bgColor = new ColorOption("bgColor", "#40000000");

    public IntegerOption chatHistory = new IntegerOption("chatHistoryLength", 100, 10, 5000);
    public ColorOption scrollbarColor = new ColorOption("scrollbarColor", "#70CCCCCC");
    public IntegerOption lineSpacing = new IntegerOption("lineSpacing", 0, 0, 10);

    public int ticks;

    public ChatHud() {
        super(320, 20);
    }

    @Override
    public void render(){

        int scrolledLines = ((AccessorChatHud) client.inGameHud.getChatHud()).getScrolledLines();
        List<ChatHudLine> visibleMessages = ((AccessorChatHud) client.inGameHud.getChatHud()).getVisibleMessages();

        if (this.client.options.chatVisibilityType != PlayerEntity.ChatVisibilityType.HIDDEN) {

            scale();
            DrawPosition pos = getPos();

            int i = getVisibleLineCount();
            int j = 0;
            int k = visibleMessages.size();
            float f = this.client.options.chatOpacity * 0.9F + 0.1F;
            if (k > 0) {

                float g = getChatScale();
                int l = MathHelper.ceil((float)getWidth() / g);
                GlStateManager.pushMatrix();

                for(int m = 0; m + scrolledLines < visibleMessages.size() && m < i; ++m) {
                    ChatHudLine chatHudLine = visibleMessages.get(m + scrolledLines);
                    if (chatHudLine != null) {
                        int n = ticks - chatHudLine.getCreationTick();
                        if (n < 200 || isChatFocused()) {
                            double d = MathHelper.clamp((1.0 - n / 200.0)*10.0, 0.0, 1.0);
                            d *= d;
                            int Opacity = isChatFocused() ? 255: (int)(255.0 * d);

                            int chatOpacity = (int)(Opacity * f);
                            ++j;
                            if (chatOpacity > 3) {
                                int y = pos.y+height - (m * (9 + lineSpacing.get()));
                                if(background.get()) {
                                    fill(pos.x, y - (9+lineSpacing.get()), pos.x + l + 4, y, bgColor.get().withAlpha(chatOpacity/2).getAsInt());
                                }
                                String string = chatHudLine.getText().asFormattedString();
                                GlStateManager.enableBlend();
                                DrawUtil.drawString(client.textRenderer, string, pos.x, (y - 8), 16777215 + (chatOpacity << 24), shadow.get());
                                GlStateManager.disableAlphaTest();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (isChatFocused()) {
                    int m = this.client.textRenderer.fontHeight;
                    GlStateManager.translatef(-3.0F, 0.0F, 0.0F);
                    int r = k * m + k;
                    int n = j * m + j;
                    int y = (pos.y+height) - scrolledLines * n / k;
                    if(((AccessorChatHud) client.inGameHud.getChatHud()).getMessages().size()> getVisibleLineCount()) {
                        int height = n * n / r;
                        fillRect(new Rectangle(pos.x, y, 2, -height), scrollbarColor.get());
                    }

                }

                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
    }

    public Text getTextAt(int x, int y){

        List<ChatHudLine> visibleMessages = ((AccessorChatHud) client.inGameHud.getChatHud()).getVisibleMessages();

        int offsetOnHudX = x - getPos().x;
        int offsetOnHudY = - (y - (getPos().y +height));


        int scrolledLines = ((AccessorChatHud) client.inGameHud.getChatHud()).getScrolledLines();


        if (offsetOnHudX >= 0 && offsetOnHudY >= 0) {
            int l = Math.min(this.getVisibleLineCount(), visibleMessages.size());
            if (offsetOnHudX <= MathHelper.floor((float)this.getWidth() / this.getChatScale()) && offsetOnHudY < (this.client.textRenderer.fontHeight + lineSpacing.get()) * l + l) {
                int m = offsetOnHudY / (this.client.textRenderer.fontHeight + lineSpacing.get()) + scrolledLines;
                if (m >= 0 && m < visibleMessages.size()) {
                    ChatHudLine chatHudLine = visibleMessages.get(m);
                    int n = 0;

                    for(Text text : chatHudLine.getText()) {
                        if (text instanceof LiteralText) {
                            n += this.client.textRenderer.getStringWidth(Texts.getRenderChatMessage(((LiteralText)text).getRawString(), false));
                            if (n > offsetOnHudX) {
                                return text;
                            }
                        }
                    }
                }

            }
        }
        return null;
    }

    @Override
    protected double getDefaultX() {
        return 0.01;
    }

    @Override
    protected float getDefaultY() {
        return 0.9F;
    }

    @Override
    public boolean tickable() {
        return true;
    }

    @Override
    public void tick() {
        width = (int) (client.options.chatWidth*320);
        height = (int) (client.options.chatHeightUnfocused*180)+11;
    }

    @Override
    public void renderPlaceholder() {
        renderPlaceholderBackground();
        scale();
        DrawPosition pos = getPos();
        if(MinecraftClient.getInstance().player!=null) {
            client.textRenderer.drawWithShadow(
                    "<" + MinecraftClient.getInstance().player.method_6344().asFormattedString() + "> OOh! There's my Chat now!",
                    pos.x + 1, pos.y + height - 9, -1
            );
        } else {
            client.textRenderer.drawWithShadow(
                    "This is where your new and fresh looking chat will be!",
                    pos.x + 1, pos.y + height - 9, -1
            );
        }
        GlStateManager.popMatrix();
        hovered=false;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public void addConfigOptions(List<OptionBase<?>> options) {
        super.addConfigOptions(options);
        options.add(shadow);
        options.add(background);
        options.add(bgColor);
        options.add(lineSpacing);
        options.add(scrollbarColor);
        options.add(chatHistory);
    }

    public boolean isChatFocused() {
        return this.client.currentScreen instanceof ChatScreen;
    }

    public int getWidth() {
        return getWidth(this.client.options.chatWidth);
    }

    public int getHeight() {
        return getHeight(this.isChatFocused() ? this.client.options.chatHeightFocused : this.client.options.chatHeightUnfocused);
    }

    public float getChatScale() {
        return this.client.options.chatScale;
    }

    public static int getWidth(float chatWidth) {
        int i = 320;
        int j = 40;
        return MathHelper.floor(chatWidth * (float)(i - j) + (float)j);
    }

    public static int getHeight(float chatHeight) {
        int i = 180;
        int j = 20;
        return MathHelper.floor(chatHeight * (float)(i - j) + (float)j);
    }

    public int getVisibleLineCount() {
        return this.getHeight() / 9;// (9+lineSpacing.get());
    }
}