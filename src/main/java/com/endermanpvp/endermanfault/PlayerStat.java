package com.endermanpvp.endermanfault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerStat {
    public int MiningSpeed = 1;
    public static PlayerStat INSTANCE;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private int tickCounter = 0;
    private static final int TICKS_PER_UPDATE = 100; // 5 seconds (20 ticks per second * 5)
    private static final Pattern MINING_SPEED_PATTERN = Pattern.compile("Mining Speed:.*?⸕([0-9,]+)");

    public PlayerStat()
    {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && mc.theWorld != null && mc.thePlayer != null) {
            tickCounter++;

            // Update mining speed every 5 seconds (100 ticks)
            if (tickCounter % TICKS_PER_UPDATE == 0) {
                updateMiningSpeed();
            }
        }
    }

    private void updateMiningSpeed() {
        if (mc.getNetHandler() == null || mc.getNetHandler().getPlayerInfoMap() == null) {
            return;
        }

        Collection<NetworkPlayerInfo> playerInfos = mc.getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : playerInfos) {
            IChatComponent displayName = playerInfo.getDisplayName();

            if (displayName != null) {
                String rawText = displayName.getFormattedText();

                // Look for mining speed pattern
                Matcher matcher = MINING_SPEED_PATTERN.matcher(rawText);
                if (matcher.find()) {
                    String miningSpeedStr = matcher.group(1);
                    // Remove commas if present
                    miningSpeedStr = miningSpeedStr.replace(",", "");

                    try {
                        int newMiningSpeed = Integer.parseInt(miningSpeedStr);
                        if (newMiningSpeed != this.MiningSpeed) {
                            this.MiningSpeed = newMiningSpeed;
                            System.out.println("Mining Speed Updated: " + this.MiningSpeed);
                        }
                        return; // Found mining speed, no need to check other entries
                    } catch (NumberFormatException e) {
                        // Continue searching if parsing fails
                    }
                }
            }
        }
    }
}
