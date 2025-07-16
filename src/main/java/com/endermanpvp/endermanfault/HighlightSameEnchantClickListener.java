package com.endermanpvp.endermanfault;


import com.endermanpvp.endermanfault.Highlightrenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HighlightSameEnchantClickListener {
    public static final Set<Integer> highlightedSlots = new HashSet<Integer>();
    @SubscribeEvent
    public void onOpenGUI(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            highlightedSlots.clear();
        }
    }
    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {


        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                Slot slot = container.getSlotUnderMouse();


                if (slot != null && slot.getHasStack()) {
                    ItemStack clickedItem = slot.getStack();
                    System.out.println("container(1): " + container.inventorySlots.getSlot(0).inventory.getName() + " " + slot.inventory.getName());
                    if(!container.inventorySlots.getSlot(0).inventory.getName().equals("Anvil")) return;

                    if(slot.inventory.getName().contains("container.inventory"))
                    {

                        highlightedSlots.clear();
                        NBTTagCompound tag = clickedItem.getTagCompound();
                        if (tag.hasKey("ExtraAttributes", Constants.NBT.TAG_COMPOUND)) {
                            NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
                            if (extra.hasKey("enchantments", Constants.NBT.TAG_COMPOUND)) {
                                NBTTagCompound enchants = extra.getCompoundTag("enchantments");
                                for (int i = 0; i < slot.inventory.getSizeInventory(); i++) {
                                    ItemStack stack = slot.inventory.getStackInSlot(i);
                                    if (stack != null) {
                                        NBTTagCompound looptag = stack.getTagCompound();
                                        if (looptag.hasKey("ExtraAttributes", Constants.NBT.TAG_COMPOUND)) {
                                            NBTTagCompound loopextra = looptag.getCompoundTag("ExtraAttributes");
                                            if(loopextra.hasKey("enchantments", Constants.NBT.TAG_COMPOUND))
                                            {

                                                if(loopextra.getCompoundTag("enchantments").equals(enchants)) {

                                                    highlightedSlots.add(i);
                                                    System.out.println("Highlighting slot: " + i + " with enchantments: " + loopextra.getCompoundTag("enchantments"));
                                                }
                                            }

                                        }
                                    }
                                }
                                highlightedSlots.remove(slot.slotNumber-45);
                                // Render highlights after updating highlightedSlots
                            }
                        }
                    } else {
                        highlightedSlots.clear();

                    }


                }
            }
        }
    }
}
