package com.endermanpvp.endermanfault.equipment;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class EquipmentFileManager {

    private static final String EQUIPMENT_FILE_NAME = "equipment_data.dat";

    /**
     * Saves equipment items to file for persistence
     * @param equipmentItems Array of ItemStacks to save
     * @param hasRecordedItems Boolean indicating if items have been recorded
     */
    public static void saveEquipmentToFile(ItemStack[] equipmentItems, boolean hasRecordedItems) {
        try {
            File equipmentFile = new File(Minecraft.getMinecraft().mcDataDir, EQUIPMENT_FILE_NAME);
            NBTTagCompound rootTag = new NBTTagCompound();
            NBTTagList equipmentList = new NBTTagList();

            for (int i = 0; i < equipmentItems.length; i++) {
                NBTTagCompound itemTag = new NBTTagCompound();
                if (equipmentItems[i] != null) {
                    equipmentItems[i].writeToNBT(itemTag);
                }
                equipmentList.appendTag(itemTag);
            }

            rootTag.setTag("EquipmentItems", equipmentList);
            rootTag.setBoolean("HasRecordedItems", hasRecordedItems);

            FileOutputStream fileOutput = new FileOutputStream(equipmentFile);
            CompressedStreamTools.writeCompressed(rootTag, fileOutput);
            fileOutput.close();

            System.out.println("[EquipmentFileManager] Equipment saved to file successfully");

        } catch (IOException e) {
            System.err.println("[EquipmentFileManager] Failed to save equipment to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads equipment items from file
     * @return EquipmentData containing the loaded items and recorded status
     */
    public static EquipmentData loadEquipmentFromFile() {
        try {
            File equipmentFile = new File(Minecraft.getMinecraft().mcDataDir, EQUIPMENT_FILE_NAME);

            if (!equipmentFile.exists()) {
                System.out.println("[EquipmentFileManager] No equipment file found, starting fresh");
                return new EquipmentData(new ItemStack[4], false);
            }

            FileInputStream fileInput = new FileInputStream(equipmentFile);
            NBTTagCompound rootTag = CompressedStreamTools.readCompressed(fileInput);
            fileInput.close();

            ItemStack[] equipmentItems = new ItemStack[4];
            boolean hasRecordedItems = false;

            if (rootTag.hasKey("EquipmentItems")) {
                NBTTagList equipmentList = rootTag.getTagList("EquipmentItems", 10); // 10 = compound tag type

                for (int i = 0; i < Math.min(equipmentList.tagCount(), equipmentItems.length); i++) {
                    NBTTagCompound itemTag = equipmentList.getCompoundTagAt(i);

                    if (itemTag.hasNoTags()) {
                        equipmentItems[i] = null;
                    } else {
                        equipmentItems[i] = ItemStack.loadItemStackFromNBT(itemTag);
                    }
                }
            }

            if (rootTag.hasKey("HasRecordedItems")) {
                hasRecordedItems = rootTag.getBoolean("HasRecordedItems");
            }

            System.out.println("[EquipmentFileManager] Equipment loaded from file successfully");
            return new EquipmentData(equipmentItems, hasRecordedItems);

        } catch (IOException e) {
            System.err.println("[EquipmentFileManager] Failed to load equipment from file: " + e.getMessage());
            e.printStackTrace();
            return new EquipmentData(new ItemStack[4], false);
        } catch (Exception e) {
            System.err.println("[EquipmentFileManager] Unexpected error loading equipment: " + e.getMessage());
            e.printStackTrace();
            return new EquipmentData(new ItemStack[4], false);
        }
    }

    /**
     * Data class to hold equipment items and their recorded status
     */
    public static class EquipmentData {
        public final ItemStack[] equipmentItems;
        public final boolean hasRecordedItems;

        public EquipmentData(ItemStack[] equipmentItems, boolean hasRecordedItems) {
            this.equipmentItems = equipmentItems;
            this.hasRecordedItems = hasRecordedItems;
        }
    }
}