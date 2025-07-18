package com.endermanpvp.endermanfault.config;


import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class ModConfig extends Config {




    @HUD(
            name = "Plushie"
    )
    @Switch(
            name = "Enable Plushie",
            description = "Render touhou plushie!"
    )
    public boolean enablePlushie = true;
    @Slider(
        name = "Plush X Offset",
        description = "Horizontal offset from the anchor position",
        min = -2000,
        max = 2000
    )
    public int plushXOffset = 0;

    @Slider(
        name = "Plush Y Offset",
        description = "Vertical offset from the anchor position",
        min = -2000,
        max = 2000
    )
    public int plushYOffset = 0;

    @Slider(
        name = "Plush Scale",
        description = "Scale of the plush model",
        min = 0.1f,
        max = 5.0f
    )
    public float plushScale = 1.0f;

    @HUD(
            name = "Plushie Conversation"
    )
    @Slider
    (
            name = "Conversation X Offset",
            description = "Horizontal offset for the conversation box",
            min = -2000,
            max = 2000
    )
    public float conversationXOffset = 0;
    @Slider
            (
                    name = "Conversation Y Offset",
                    description = "Vertical offset for the conversation box",
                    min = -2000,
                    max = 2000
            )
    public float conversationYOffset = 0;
    @Slider
            (
                    name = "Conversation Scale",
                    description = "Vertical offset for the conversation box",
                    min = 0,
                    max = 5
            )
    public float conversationScale = 1;
    @HUD(name = "Superpair Support")
    @Switch
            (
                    name = "Enable Superpair Support",
                    description = "Render items you have clicked"
            )
    public boolean EnableSuperpairSupport = true;

    public ModConfig() {
        super(new Mod("Enderman Fault", ModType.SKYBLOCK,"assets/endermanfault/icon.png"), "endermanfault.json");
        initialize();
    }
}
