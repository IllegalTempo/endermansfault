package com.endermanpvp.endermanfault.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static com.endermanpvp.endermanfault.config.SettingToggle.SCALE;

public class CustomButton extends GuiButton {
    protected final ResourceLocation buttonTexture;
    protected final IButtonAction onClickAction;

    // Interface for button actions (Java 6 compatible)
    public interface IButtonAction {
        void execute();
    }


    public CustomButton(int buttonId, int x, int y, int width, int height, String buttonText, IButtonAction onClickAction) {
        super(buttonId, x, y, width, height, buttonText);
        this.buttonTexture = new ResourceLocation("endermanfault", "textures/ui/button.png");
        this.onClickAction = onClickAction;
    }

    public CustomButton(int buttonId, int x, int y, String buttonText, IButtonAction onClickAction) {
        this(buttonId, x, y, 200, 20, buttonText, onClickAction); // Default Minecraft button size
    }

    public void executeAction() {
        if (this.onClickAction != null) {
            this.onClickAction.execute();
        }
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
            mc.getTextureManager().bindTexture(buttonTexture);

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
                int textColor = 0xFFFFFF; // Default button text color
                int centerX = this.xPosition + this.width / 2;
                int centerY = this.yPosition + (this.height - 8) / 2;




                this.drawCenteredString(mc.fontRendererObj, this.displayString, centerX, centerY, textColor);

            }
        }
    }
}
