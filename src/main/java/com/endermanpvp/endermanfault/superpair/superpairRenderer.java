package com.endermanpvp.endermanfault.superpair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.endermanpvp.endermanfault.superpair.superpairmain.DisplayedItems;

public class superpairRenderer {
    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {

        if (event.gui instanceof GuiContainer ) {
            String invname = ((GuiContainer) event.gui).inventorySlots.getSlot(0).inventory.getName();
            if(invname.contains("Superpairs") && !invname.contains("Stakes"))
            {
                GuiContainer container = (GuiContainer) event.gui;

                for (Slot slot : container.inventorySlots.inventorySlots) {

                        if(slot.getStack() == null) return;
                        if(DisplayedItems.containsKey(slot.slotNumber))
                        {
                            ItemStack itemstack = DisplayedItems.get(slot.slotNumber);
                            if (itemstack != null) {
                                int x = (event.gui.width - 176) / 2 + slot.xDisplayPosition;
                                int y = (event.gui.height - 222) / 2 + slot.yDisplayPosition;

                                GlStateManager.pushMatrix();
                                GlStateManager.translate(0, 0, 200); // Bring to front

                                // Draw a background to cover the original item

                                RenderHelper.enableGUIStandardItemLighting();
                                Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemstack, x, y);
                                RenderHelper.disableStandardItemLighting();

                                GlStateManager.popMatrix();
                            }
                        } else {
                            if(slot.getStack().getItem() != Item.getItemFromBlock(Blocks.stained_glass) && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.bookshelf))
                            {
                                DisplayedItems.put(slot.slotNumber, slot.getStack());
                            }


                        }

                    }
                }
            }

        }
    }

