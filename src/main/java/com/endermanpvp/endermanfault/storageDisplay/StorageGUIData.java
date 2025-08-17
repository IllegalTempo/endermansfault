package com.endermanpvp.endermanfault.storageDisplay;

import com.endermanpvp.endermanfault.config.ModConfig;
import com.endermanpvp.endermanfault.equipment.EquipmentFileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StorageGUIData {
    private static final String STORAGE_FILE_NAME = "storage_data.dat";
    public List<Storage> storages = new ArrayList<Storage>();

    public static class Storage{
        public boolean IsEnderChest;
        public int StorageNum;
        public Slot[] contents;
        public Storage(boolean isEnderChest, int storageNum, Slot[] contents) {
            IsEnderChest = isEnderChest;
            StorageNum = storageNum;
            this.contents = contents;
        }

    }
    private int FindStorage(int storageNum, boolean IsEnderChest) {
        for (int i = 0; i < storages.size(); i++) {
            Storage storage = storages.get(i);
            if (storage.StorageNum == storageNum && storage.IsEnderChest == IsEnderChest) {
                return i;
            }
        }
        return -1; // Not found
    }
    public void AddStorage(GuiContainer container, boolean IsEnderChest, int storageNum) {
        // Save container slots (not player inventory slots)
        // Dynamically calculate container size by finding where player inventory starts
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = totalSlots - 36; // Player inventory is always 36 slots, so container = total - 36

        Slot[] contents = new Slot[containerSlots];

        // Take the container slots (0 to containerSlots-1), NOT the player inventory slots
        for (int i = 0; i < containerSlots; i++) {
            if (i < totalSlots) {
                contents[i] = container.inventorySlots.getSlot(i);
            }
        }

        storages.add(new Storage(IsEnderChest, storageNum, contents));

        // Automatically save after adding storage
        saveStorages();
    }
    public void UpdateStorage(GuiContainer container, int storageIndex) {
        // Save container slots (not player inventory slots)
        // Dynamically calculate container size by finding where player inventory starts
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = totalSlots - 36; // Player inventory is always 36 slots, so container = total - 36

        Slot[] storageContents = new Slot[containerSlots];

        // Take the container slots (0 to containerSlots-1), NOT the player inventory slots
        for (int i = 0; i < containerSlots; i++) {
            if (i < totalSlots) {
                storageContents[i] = container.inventorySlots.getSlot(i);
            }
        }

        storages.get(storageIndex).contents = storageContents;
        // Automatically save after updating storage
        saveStorages();
    }
    private boolean Current_IsEnderChest;
    private int Current_StorageNum;
    private void UpdateStorageData(GuiContainer container)
    {
        String invName = container.inventorySlots.getSlot(0).inventory.getName();
        String substring = "";
        if(invName.startsWith("Ender Chest"))
        {
            Current_IsEnderChest = true;
            substring = invName.substring(invName.indexOf("(")+1, invName.indexOf("/"));
            Current_StorageNum = Integer.parseInt(substring);
            System.out.println(substring);
            int StorageIndex = FindStorage(Current_StorageNum, true);
            if(StorageIndex == -1)
            {
                AddStorage(container, true, Current_StorageNum);
            } else {
                UpdateStorage(container, StorageIndex);
            }

        } else
        if(invName.contains("Backpack"))
        {
            Current_IsEnderChest = false;
            substring = invName.substring(invName.indexOf("#")+1,invName.length()-1);
            Current_StorageNum = Integer.parseInt(substring);
            System.out.println(substring);
            int StorageIndex = FindStorage(Current_StorageNum, false);
            if(StorageIndex == -1)
            {
                AddStorage(container, false, Current_StorageNum);
            } else {
                UpdateStorage(container, StorageIndex);
            }
        }
    }

    @SubscribeEvent
    public void onStorageGUIOpen(GuiOpenEvent event)
    {
        // Check if storage display is enabled in config
        if (!ModConfig.getInstance().getBoolean("enable_storage", true)) {
            return; // Exit early if storage system is disabled
        }

        if(event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;
            if (container.inventorySlots.getSlot(0).inventory.getName().startsWith("Ender Chest") ||
                    container.inventorySlots.getSlot(0).inventory.getName().contains("Backpack")) {
                UpdateStorageData(container);
            }

        }

    }


    /**
     * Save the current storages to an NBT file using compressed streams
     * Mimics EquipmentFileManager approach for reliability
     */
    public void saveStorages() {
        try {
            File storageFile = new File(Minecraft.getMinecraft().mcDataDir, STORAGE_FILE_NAME);
            NBTTagCompound rootTag = new NBTTagCompound();
            NBTTagList storagesList = new NBTTagList();

            for (Storage storage : storages) {
                NBTTagCompound storageTag = new NBTTagCompound();
                storageTag.setBoolean("IsEnderChest", storage.IsEnderChest);
                storageTag.setInteger("StorageNum", storage.StorageNum);

                NBTTagList itemsList = new NBTTagList();
                // Only save items starting from index 9 (skip player inventory slots 0-8)
                for (int i = 9; i < storage.contents.length; i++) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    Slot slot = storage.contents[i];

                    if (slot != null && slot.getStack() != null) {
                        slot.getStack().writeToNBT(itemTag);
                    }
                    // Even empty slots get saved to maintain slot positions
                    itemsList.appendTag(itemTag);
                }

                storageTag.setTag("Items", itemsList);
                storagesList.appendTag(storageTag);
            }

            rootTag.setTag("StorageList", storagesList);

            FileOutputStream fileOutput = new FileOutputStream(storageFile);
            CompressedStreamTools.writeCompressed(rootTag, fileOutput);
            fileOutput.close();

            System.out.println("[StorageGUIData] Storage data saved to file successfully");

        } catch (IOException e) {
            System.err.println("[StorageGUIData] Failed to save storage data to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load storages from NBT file using compressed streams
     * Mimics EquipmentFileManager approach for reliability
     */
    public void loadStorages() {
        try {
            File storageFile = new File(Minecraft.getMinecraft().mcDataDir, STORAGE_FILE_NAME);

            if (!storageFile.exists()) {
                System.out.println("[StorageGUIData] No storage file found, starting fresh");
                return;
            }

            FileInputStream fileInput = new FileInputStream(storageFile);
            NBTTagCompound rootTag = CompressedStreamTools.readCompressed(fileInput);
            fileInput.close();

            storages.clear(); // Clear existing storages

            if (rootTag.hasKey("StorageList")) {
                NBTTagList storagesList = rootTag.getTagList("StorageList", 10); // 10 = compound tag type

                for (int i = 0; i < storagesList.tagCount(); i++) {
                    NBTTagCompound storageTag = storagesList.getCompoundTagAt(i);

                    boolean isEnderChest = storageTag.getBoolean("IsEnderChest");
                    int storageNum = storageTag.getInteger("StorageNum");

                    if (storageTag.hasKey("Items")) {
                        NBTTagList itemsList = storageTag.getTagList("Items", 10);
                        // Create array with proper size: 9 empty slots + saved items
                        Slot[] contents = new Slot[9 + itemsList.tagCount()];

                        // Fill slots 0-8 with null (player inventory slots that we don't save)
                        for (int j = 0; j < 9; j++) {
                            contents[j] = null;
                        }

                        // Load the saved items starting from index 9
                        for (int j = 0; j < itemsList.tagCount(); j++) {
                            NBTTagCompound itemTag = itemsList.getCompoundTagAt(j);

                            if (!itemTag.hasNoTags()) {
                                ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
                                // Create a temporary slot to hold the item
                                // Note: This is for display purposes only, real slots need inventory references
                                contents[9 + j] = new TemporarySlot(stack);
                            } else {
                                contents[9 + j] = null; // Empty slot
                            }
                        }

                        storages.add(new Storage(isEnderChest, storageNum, contents));
                    }
                }
            }

            System.out.println("[StorageGUIData] Storage data loaded from file successfully. Loaded " + storages.size() + " storages");

        } catch (IOException e) {
            System.err.println("[StorageGUIData] Failed to load storage data from file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[StorageGUIData] Unexpected error loading storage data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Temporary slot class for holding ItemStacks when loading from file
     * Used for display purposes only since real slots need inventory references
     */
    private static class TemporarySlot extends Slot {
        private ItemStack stack;

        public TemporarySlot(ItemStack stack) {
            super(null, 0, 0, 0); // No inventory reference needed for display
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void putStack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean getHasStack() {
            return stack != null;
        }
    }
}
