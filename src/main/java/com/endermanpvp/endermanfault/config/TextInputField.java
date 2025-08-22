package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.MyDataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import static com.endermanpvp.endermanfault.config.SettingToggle.SCALE;

public class TextInputField extends GuiButton {
    private GuiTextField textField;
    private final String configField;
    private final ResourceLocation inputTexture;
    MyDataType<String> StringConfig;
    private boolean isFocused = false;
    private final String placeholderText;

    public TextInputField(int buttonId, int x, int y, String labelText, String configField, String placeholderText) {
        super(buttonId, x, y, (int)(512*SCALE), (int)(64*SCALE), labelText);
        this.configField = configField;
        this.placeholderText = placeholderText;
        // Use existing button texture instead of non-existent textinput texture
        this.inputTexture = new ResourceLocation("endermanfault", "textures/ui/button.png");

        // Initialize the text field
        this.textField = new GuiTextField(buttonId, Minecraft.getMinecraft().fontRendererObj, x + 5, y + 5, this.width - 10, this.height - 10);
        this.textField.setMaxStringLength(100);
        // Handle case where config field might not exist yet
        StringConfig = AllConfig.INSTANCE.StringConfig.get(configField);
        this.textField.setText(StringConfig.data != null ? StringConfig.data : "");
        this.textField.setCanLoseFocus(true);
    }

    public TextInputField(int buttonId, int x, int y, String labelText, String configField) {
        this(buttonId, x, y, labelText, configField, "Enter text...");
    }

    public void updateConfig() {
        StringConfig.data = this.textField.getText();
    }


    public void setFocused(boolean focused) {
        this.isFocused = focused;
        this.textField.setFocused(focused);
        if (focused) {
            // Set this as the typing target in ConfigGUI
            if (ConfigGUI.INSTANCE != null) {
                ConfigGUI.INSTANCE.typingTo = this;
            }
        }
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (this.isFocused) {
            boolean wasChanged = this.textField.textboxKeyTyped(typedChar, keyCode);
            if (wasChanged) {
                updateConfig();
            }

            // Handle Enter key to unfocus
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                setFocused(false);
                if (ConfigGUI.INSTANCE != null) {
                    ConfigGUI.INSTANCE.typingTo = null;
                }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean wasClicked = mouseX >= this.xPosition && mouseX <= this.xPosition + this.width &&
                           mouseY >= this.yPosition && mouseY <= this.yPosition + this.height;

        setFocused(wasClicked);
        if (wasClicked) {
            this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void updateCursorCounter() {
        if (this.isFocused) {
            this.textField.updateCursorCounter();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // Check if mouse is hovering over the button
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                          mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw input field background using simple rectangle instead of texture
            // This creates a clean input field appearance without needing a texture file
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();

            // Background fill
            int backgroundColor = this.isFocused ? 0xFF2A2A2A : 0xFF1A1A1A;
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, backgroundColor);

            // Border
            int borderColor = this.isFocused ? 0xFF4A9EFF : (this.hovered ? 0xFF666666 : 0xFF333333);
            // Top border
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 1, borderColor);
            // Bottom border
            this.drawRect(this.xPosition, this.yPosition + this.height - 1, this.xPosition + this.width, this.yPosition + this.height, borderColor);
            // Left border
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + 1, this.yPosition + this.height, borderColor);
            // Right border
            this.drawRect(this.xPosition + this.width - 1, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, borderColor);

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Update text field position to match button position
            this.textField.xPosition = this.xPosition + 5;
            this.textField.yPosition = this.yPosition + (this.height - 10) / 2;
            this.textField.width = this.width - 10;
            this.textField.height = 12;

            // Draw the text field content
            if (this.textField.getText().isEmpty() && !this.isFocused) {
                // Draw placeholder text
                int centerY = this.yPosition + (this.height - 8) / 2;
                mc.fontRendererObj.drawString(this.placeholderText, this.xPosition + 8, centerY, 0x888888);
            } else {
                // Draw the actual text content
                String displayText = this.textField.getText();
                int textColor = this.isFocused ? 0xFFFFFF : 0xCCCCCC;
                int centerY = this.yPosition + (this.height - 8) / 2;
                mc.fontRendererObj.drawString(displayText, this.xPosition + 8, centerY, textColor);

                // Draw cursor if focused
                if (this.isFocused && this.textField.getVisible()) {
                    int cursorPos = this.textField.getCursorPosition();
                    String textBeforeCursor = displayText.substring(0, Math.min(cursorPos, displayText.length()));
                    int cursorX = this.xPosition + 8 + mc.fontRendererObj.getStringWidth(textBeforeCursor);

                    // Simple blinking cursor
                    if ((System.currentTimeMillis() / 500) % 2 == 0) {
                        this.drawRect(cursorX, centerY, cursorX + 1, centerY + 8, 0xFFFFFFFF);
                    }
                }
            }

            // Draw label text above the input field
            if (this.displayString != null && !this.displayString.isEmpty()) {
                int labelY = this.yPosition - 12;
                mc.fontRendererObj.drawString(this.displayString, this.xPosition, labelY, 0xFFFFFF);
            }
        }
    }
}
