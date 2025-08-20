package com.endermanpvp.endermanfault.superpair;

import com.endermanpvp.endermanfault.DataType.Toggle;
import com.endermanpvp.endermanfault.config.AllConfig;
import com.endermanpvp.endermanfault.config.ModConfig;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class superpairmain {
    public static HashMap<Integer, ItemStack> DisplayedItems = new HashMap<Integer,ItemStack>();
    public final Toggle Toggle = AllConfig.INSTANCE.BooleanConfig.get("toggle_superpair");

    public superpairRenderer superpairRenderer = new superpairRenderer();
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if(!Toggle.data) return;
        DisplayedItems.clear();

        if (e.gui instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) e.gui;

            if (gui.inventorySlots.getSlot(0).inventory.getName().contains("Superpairs")) {
                MinecraftForge.EVENT_BUS.register(superpairRenderer);
            }
        } else if (e.gui == null) {
            DisplayedItems.clear();

            // GUI is closing
            MinecraftForge.EVENT_BUS.unregister(superpairRenderer);
        }
    }
}
