package com.endermanpvp.endermanfault;

import com.endermanpvp.endermanfault.config.ModConfig;
import com.endermanpvp.endermanfault.enchantbookcrafter.HighlightSameEnchantClickListener;
import com.endermanpvp.endermanfault.enchantbookcrafter.Highlightrenderer;
import com.endermanpvp.endermanfault.plush.MouseInputHandler;
import com.endermanpvp.endermanfault.plush.PlushRenderer;
import com.endermanpvp.endermanfault.plush.conversation.conversationRenderer;
import com.endermanpvp.endermanfault.superpair.superpairmain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = main.MODID, version = main.VERSION)
public class main
{
    public static final String MODID = "endermanfault";
    public static final String VERSION = "1.0";

    public static ModConfig config;





    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        config = new ModConfig();
        MinecraftForge.EVENT_BUS.register(new HighlightSameEnchantClickListener());

        PlushRenderer plushRenderer = new PlushRenderer();
        plushRenderer.init();
        MinecraftForge.EVENT_BUS.register(plushRenderer);
        MinecraftForge.EVENT_BUS.register(new superpairmain());
        MinecraftForge.EVENT_BUS.register(new MouseInputHandler(plushRenderer));
        MinecraftForge.EVENT_BUS.register(this);
        // Register the conversation renderer
        MinecraftForge.EVENT_BUS.register(conversationRenderer.getInstance());
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiContainer && !HighlightSameEnchantClickListener.highlightedSlots.isEmpty()) {
            GuiContainer container = (GuiContainer) event.gui;

            // Check if we're still in an anvil
            if (container.inventorySlots.getSlot(0).inventory.getName().equals("Anvil")) {
                Highlightrenderer.renderHighlightedSlots(container);
            }
        }
    }
}
