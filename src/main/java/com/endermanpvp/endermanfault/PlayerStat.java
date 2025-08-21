package com.endermanpvp.endermanfault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlayerStat {
    public int MiningSpeed = 1;
    public int Ping;
    public static PlayerStat INSTANCE;
    private int tickCounter = 0;
    private static final int PING_UPDATE_INTERVAL = 100; // 5 seconds (20 ticks per second * 5)

    public PlayerStat()
    {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void OnPlayerOpenEquipmentPage(GuiScreenEvent event)
    {
        if(event.gui instanceof GuiContainer)
        {
            GuiContainer gui = (GuiContainer) event.gui;
            String invName = gui.inventorySlots.getSlot(0).inventory.getName();
            if (invName.contains("Your Equipment and Stats")) {
                //get 16th slot
                if (gui.inventorySlots.getSlot(16) != null && gui.inventorySlots.getSlot(16).getHasStack()) {
                    // Parse Mining Speed from item lore
                    this.MiningSpeed = parseMiningSpeedFromLore(gui.inventorySlots.getSlot(16).getStack());
                } else {
                    this.MiningSpeed = 1; // Default value if no item is found
                }
            }
        }
    }

    private int parseMiningSpeedFromLore(net.minecraft.item.ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display")) {
            net.minecraft.nbt.NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");
            if (display.hasKey("Lore")) {
                net.minecraft.nbt.NBTTagList lore = display.getTagList("Lore", 8); // 8 = String type
                for (int i = 0; i < lore.tagCount(); i++) {
                    String loreLine = lore.getStringTagAt(i);
                    if (loreLine.contains("Mining Speed")) {
                        // Extract the number after "Mining Speed §f"
                        String[] parts = loreLine.split("Mining Speed §f");
                        if (parts.length > 1) {
                            String numberPart = parts[1].trim();
                            // Remove any color codes and commas
                            numberPart = numberPart.replaceAll("§[0-9a-fk-or]", "").replaceAll(",", "");
                            try {
                                return Integer.parseInt(numberPart);
                            } catch (NumberFormatException e) {
                                // If parsing fails, try to extract just the digits
                                numberPart = numberPart.replaceAll("[^0-9]", "");
                                if (!numberPart.isEmpty()) {
                                    return Integer.parseInt(numberPart);
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0; // Default if not found
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= PING_UPDATE_INTERVAL) {
                updatePing();
                tickCounter = 0;
            }
        }
    }

    private void updatePing()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null) {
            NetHandlerPlayClient connection = mc.thePlayer.sendQueue;
            if (connection != null) {
                // Get ping from the network handler
                this.Ping = connection.getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
            }
        }
    }
}
