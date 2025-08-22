package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.MyDataType;
import com.endermanpvp.endermanfault.DataType.MyInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import static com.endermanpvp.endermanfault.config.SettingToggle.SCALE;

public class IntInputField extends GuiButton {
    private GuiTextField textField;
    private final String configField;
    private boolean isFocused = false;
    private final String placeholderText;
    private final int minValue;
    private final int maxValue;
    private boolean hasError = false;
    MyDataType<Integer> existingValue;

    public IntInputField(int buttonId, int x, int y, String labelText, String configField, String placeholderText, int minValue, int maxValue) {
        super(buttonId, x, y, (int)(512*SCALE), (int)(64*SCALE), labelText);
        this.configField = configField;
        this.placeholderText = placeholderText;
        this.minValue = minValue;
        this.maxValue = maxValue;

        // Initialize the text field
        this.textField = new GuiTextField(buttonId, Minecraft.getMinecraft().fontRendererObj, x + 5, y + 5, this.width - 10, this.height - 10);
        this.textField.setMaxStringLength(20);

        // Handle case where config field might not exist yet
        existingValue = AllConfig.INSTANCE.IntConfig.get(configField);
        String displayText = existingValue != null ? String.valueOf(existingValue.data) : "";
        this.textField.setText(displayText);
        this.textField.setCanLoseFocus(true);
    }

    public IntInputField(int buttonId, int x, int y, String labelText, String configField, int minValue, int maxValue) {
        this(buttonId, x, y, labelText, configField, "0", minValue, maxValue);
    }

    public IntInputField(int buttonId, int x, int y, String labelText, String configField) {
        this(buttonId, x, y, labelText, configField, "0", Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private boolean isValidInteger(String text) {
        if (text.isEmpty()) return true; // Allow empty for editing

        try {
            int value = Integer.parseInt(text);
            return value >= minValue && value <= maxValue;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void updateConfig() {
        String text = this.textField.getText();
        if (text.isEmpty()) {
            // Don't update config when field is empty during editing
            // Only clear error state
            this.hasError = false;
        } else if (isValidInteger(text)) {
            int value = Integer.parseInt(text);
            existingValue.data = value;
            this.hasError = false;
        } else {
            this.hasError = true;
        }
    }





    public void setFocused(boolean focused) {
        this.isFocused = focused;
        this.textField.setFocused(focused);
        if (focused) {
            // Set this as the typing target in ConfigGUI
            if (ConfigGUI.INSTANCE != null) {
                ConfigGUI.INSTANCE.typingTo = this;
            }
        } else {
            // Validate and fix value when losing focus
            if (this.hasError || this.textField.getText().isEmpty()) {
                int fallbackValue = existingValue != null ? existingValue.data : minValue;
                this.textField.setText(String.valueOf(fallbackValue));
                this.hasError = false;
            }
        }
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (this.isFocused) {
            // Only allow numbers, minus sign (at start), and control characters
            if (Character.isDigit(typedChar) ||
                (typedChar == '-' && this.textField.getCursorPosition() == 0 && minValue < 0) ||
                typedChar == '\b' || typedChar == 127) { // backspace and delete

                boolean wasChanged = this.textField.textboxKeyTyped(typedChar, keyCode);
                if (wasChanged) {
                    updateConfig();
                }
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

            // Draw input field background using simple rectangle
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();

            // Background fill - different color if there's an error
            int backgroundColor;
            if (this.hasError) {
                backgroundColor = 0xFF3A1A1A; // Dark red for errors
            } else if (this.isFocused) {
                backgroundColor = 0xFF2A2A2A;
            } else {
                backgroundColor = 0xFF1A1A1A;
            }
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, backgroundColor);

            // Border - red if error, blue if focused, gray otherwise
            int borderColor;
            if (this.hasError) {
                borderColor = 0xFFFF4444; // Red for errors
            } else if (this.isFocused) {
                borderColor = 0xFF4A9EFF; // Blue for focus
            } else if (this.hovered) {
                borderColor = 0xFF666666; // Gray for hover
            } else {
                borderColor = 0xFF333333; // Dark gray normal
            }

            // Draw borders
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 1, borderColor);
            this.drawRect(this.xPosition, this.yPosition + this.height - 1, this.xPosition + this.width, this.yPosition + this.height, borderColor);
            this.drawRect(this.xPosition, this.yPosition, this.xPosition + 1, this.yPosition + this.height, borderColor);
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
                int placeholderColor = this.hasError ? 0xFFAA6666 : 0x888888;
                mc.fontRendererObj.drawString(this.placeholderText, this.xPosition + 8, centerY, placeholderColor);
            } else {
                // Draw the actual text content
                String displayText = this.textField.getText();
                int textColor;
                if (this.hasError) {
                    textColor = 0xFFFF6666; // Light red for error text
                } else if (this.isFocused) {
                    textColor = 0xFFFFFF; // White when focused
                } else {
                    textColor = 0xCCCCCC; // Light gray normally
                }

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
                int labelColor = this.hasError ? 0xFFFF6666 : 0xFFFFFF;
                mc.fontRendererObj.drawString(this.displayString, this.xPosition, labelY, labelColor);
            }

            // Draw range hint below the field
            if (minValue != Integer.MIN_VALUE || maxValue != Integer.MAX_VALUE) {
                String rangeText = "Range: " +
                    (minValue == Integer.MIN_VALUE ? "∞" : String.valueOf(minValue)) +
                    " to " +
                    (maxValue == Integer.MAX_VALUE ? "∞" : String.valueOf(maxValue));
                int hintY = this.yPosition + this.height + 2;
                mc.fontRendererObj.drawString(rangeText, this.xPosition, hintY, 0x888888);
            }
        }
    }
}
