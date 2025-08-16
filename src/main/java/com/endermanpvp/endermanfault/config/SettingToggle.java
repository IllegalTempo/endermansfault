package com.endermanpvp.endermanfault.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class SettingToggle extends GuiButton {
    private final ResourceLocation textureOff;
    private final ResourceLocation textureToggled;
    private final String configField;
    public static float SCALE = 0.5F;
    public SettingToggle(int buttonId, int x, int y, String buttonText, String configField) {

        super(buttonId, x, y, (int)(512*SCALE), (int)(64*SCALE), buttonText);
        this.textureOff = new ResourceLocation("endermanfault", "textures/ui/toggleoff.png");
        this.textureToggled = new ResourceLocation("endermanfault", "textures/ui/toggleon.png");
        this.configField = configField;
    }

    private boolean getConfigValue(String configField) {
        return ModConfig.getInstance().getBoolean(configField, false);
    }

    private void setConfigValue(String configField, boolean value) {
        ModConfig.getInstance().setBoolean(configField, value);
    }

    public void toggleConfig() {
        boolean currentValue = getConfigValue(this.configField);
        setConfigValue(this.configField, !currentValue);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // Check if mouse is hovering over the button
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                          mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();

            // Apply scaling: 0.5 for width, 0.125 for height (0.5 * 0.25)
            GlStateManager.translate(this.xPosition, this.yPosition, 0);
            GlStateManager.scale(2F*SCALE, 0.25F * SCALE, 1.0F);

            // Determine which texture to use based on config value
            ResourceLocation texture = getConfigValue(this.configField) ? this.textureToggled : this.textureOff;
            mc.getTextureManager().bindTexture(texture);

            // Draw the button texture at original size (it will be scaled by the matrix)
            this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);

            GlStateManager.popMatrix();

            // Apply dark overlay when hovered
            if (this.hovered) {
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F); // Dark overlay with 40% opacity
                this.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0x66000000);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            // Optional: Draw button text if needed (draw after popping matrix to avoid scaling text)
            if (this.displayString != null && !this.displayString.isEmpty()) {
                int textColor = 0x101010; // Black text color (much darker)

                this.drawCenteredString(mc.fontRendererObj, this.displayString,
                    this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, textColor);
            }
        }
    }
}
