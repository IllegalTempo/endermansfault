package com.endermanpvp.endermanfault.config;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.List;

public class CommandOpenConfig extends CommandBase {
    @Override
    public String getCommandName() {
        return "ef";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ef";
    }

    @Override
    public int getRequiredPermissionLevel() {
        // client-side command, no perms
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        System.out.println("EndermanFault config opened.");
        System.out.println("[Command] thePlayer: " + Minecraft.getMinecraft().thePlayer);
        System.out.println("[Command] currentScreen before: " + Minecraft.getMinecraft().currentScreen);
        // Delay opening the GUI by 1 tick to avoid chat screen closing issue
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50); // ~1 tick (50ms) //FUCK YOUUUUUUUUUUUUUUUUUUUUUUUUU
                } catch (InterruptedException ignored) {}
                Minecraft.getMinecraft().addScheduledTask(()-> {
                        System.out.println("[Command] currentScreen scheduled before: " + Minecraft.getMinecraft().currentScreen);
                        Minecraft.getMinecraft().displayGuiScreen(ConfigGUI.INSTANCE);
                        System.out.println("[Command] currentScreen scheduled after: " + Minecraft.getMinecraft().currentScreen);

                });
            }
        }).start();
    }

    // Tab completion: none
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
