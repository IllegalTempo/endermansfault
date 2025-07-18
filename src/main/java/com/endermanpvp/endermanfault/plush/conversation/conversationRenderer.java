package com.endermanpvp.endermanfault.plush.conversation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class conversationRenderer {
    private static final ResourceLocation CHATBOX_TEXTURE = new ResourceLocation("endermanfault", "textures/ui/chatbox.png");
    private static conversationRenderer instance;

    private String currentMessage = "";
    private boolean isVisible = false;
    private int chatboxWidth = 300;
    private int chatboxHeight = 100;
    private FontRenderer fontRenderer;

    public conversationRenderer() {
        this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    public static conversationRenderer getInstance() {
        if (instance == null) {
            instance = new conversationRenderer();
        }
        return instance;
    }

    // Main dialogue method
    public void showDialogue(String text) {
        this.currentMessage = text;
        this.isVisible = true;
    }

    public void hideDialogue() {
        this.isVisible = false;
        this.currentMessage = "";
    }
    public void showDialogue(String text, final int timeInSeconds) {
        this.currentMessage = text;
        this.isVisible = true;

        // Schedule hiding the dialogue after the specified time
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeInSeconds * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hideDialogue();
            }
        }).start();
    }
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && isVisible) {
            renderChatBox();
        }
    }

    private void renderChatBox() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        // Calculate position (center bottom of screen)
        int x = (scaledResolution.getScaledWidth() - chatboxWidth) / 2;
        int y = scaledResolution.getScaledHeight() - chatboxHeight - 50;

        // Enable blending for transparency
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Bind and render chatbox texture
        mc.getTextureManager().bindTexture(CHATBOX_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the chatbox background
        mc.ingameGUI.drawTexturedModalRect(x, y, 0, 0, chatboxWidth, chatboxHeight);

        // Render text
        if (!currentMessage.isEmpty()) {
            renderText(currentMessage, x, y);
        }

        GlStateManager.disableBlend();
    }

    private void renderText(String text, int x, int y) {
        // Text padding from edges
        int textPadding = 10;
        int textX = x + textPadding;
        int textY = y + textPadding;
        int maxWidth = chatboxWidth - (textPadding * 2);

        // Split text into lines that fit within the chatbox
        List<String> lines = wrapText(text, maxWidth);

        // Render each line
        for (int i = 0; i < lines.size(); i++) {
            fontRenderer.drawString(lines.get(i), textX, textY + (i * 10), 0xFFFFFF);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<String>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (fontRenderer.getStringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // Utility methods
    public boolean isVisible() {
        return isVisible;
    }

    public void setChatboxSize(int width, int height) {
        this.chatboxWidth = width;
        this.chatboxHeight = height;
    }
}
