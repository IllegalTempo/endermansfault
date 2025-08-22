package com.endermanpvp.endermanfault.config;

public class SubConfigButton extends CustomButton{

    public SubConfigButton(int SubSettingIndex,int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText, ()->{
            ConfigGUI.INSTANCE.SubSettingIndex = SubSettingIndex;
        });
    }
}
