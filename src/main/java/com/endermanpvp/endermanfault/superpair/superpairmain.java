package com.endermanpvp.endermanfault.superpair;

import com.endermanpvp.endermanfault.main;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class superpairmain {
    public static HashMap<Integer, ItemStack> DisplayedItems = new HashMap<Integer,ItemStack>();
    public superpairRenderer superpairRenderer = new superpairRenderer();
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if(!main.config.EnableSuperpairSupport) return;
        DisplayedItems.clear();

        if (e.gui instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) e.gui;

            if (gui.inventorySlots.getSlot(0).inventory.getName().contains("Superpairs")) {
                MinecraftForge.EVENT_BUS.register(superpairRenderer);
            }
        } else if (e.gui == null) {
            DisplayedItems.clear();

            // GUI is closingp
            MinecraftForge.EVENT_BUS.unregister(superpairRenderer);
        }
    }
}
