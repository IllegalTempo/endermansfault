package com.endermanpvp.endermanfault.config;

import net.minecraft.client.Minecraft;

public class PositionSelectorButton extends CustomButton{

    public PositionSelectorButton(int buttonId, int x, int y, int width, int height, String buttonText, String MainConfigField) {
        super(buttonId, x, y, width, height, buttonText, () -> Minecraft.getMinecraft().displayGuiScreen(new PositionSelector(ConfigGUI.INSTANCE, buttonText, MainConfigField, width / 2, height / 2, 1.0f)));
    }
}
