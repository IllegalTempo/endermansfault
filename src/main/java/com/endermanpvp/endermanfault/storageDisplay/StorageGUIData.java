package com.endermanpvp.endermanfault.storageDisplay;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class StorageGUIData {
    private static final String STORAGE_FILE_PATH = "config/endermanfault_storage.json";
    public List<Storage> storages = new ArrayList<Storage>();
    private class Storage{
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
        String invName = container.inventorySlots.getSlot(0).inventory.getName();
        Slot[] contents = new Slot[container.inventorySlots.inventorySlots.size()];
        for (int i = 0; i < contents.length; i++) {
            contents[i] = container.inventorySlots.getSlot(i);
        }
        storages.add(new Storage(IsEnderChest, storageNum, contents));

        // Automatically save after adding storage
        saveStorages();
    }
    public void UpdateStorage(GuiContainer container, int storageNum, boolean IsEnderChest) {
        storages.get(FindStorage(storageNum,IsEnderChest)).contents = container.inventorySlots.inventorySlots.toArray(new Slot[0]);
        // Automatically save after updating storage
        saveStorages();
    }
    private boolean Current_IsEnderChest;
    private int Current_StorageNum;
    @SubscribeEvent
    public void onStorageGUIOpen(GuiOpenEvent event)
    {
        if(event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;
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
                }

            } else
            if(invName.contains("Backpack"))
            {
                Current_IsEnderChest = false;
                substring = invName.substring(invName.indexOf("#")+1);
                Current_StorageNum = Integer.parseInt(substring);
                System.out.println(substring);
                int StorageIndex = FindStorage(Current_StorageNum, false);
                if(StorageIndex == -1)
                {
                    AddStorage(container, false, Current_StorageNum);
                }
            }


        }

    }


    // Serializable version of storage data
    private static class SerializableStorage {
        public boolean IsEnderChest;
        public int StorageNum;
        public SerializableItem[] contents;

        public SerializableStorage(boolean isEnderChest, int storageNum, SerializableItem[] contents) {
            this.IsEnderChest = isEnderChest;
            this.StorageNum = storageNum;
            this.contents = contents;
        }
    }

    // Serializable version of item data
    private static class SerializableItem {
        public String itemName;
        public int stackSize;
        public int damage;
        public String nbtData;

        public SerializableItem(String itemName, int stackSize, int damage, String nbtData) {
            this.itemName = itemName;
            this.stackSize = stackSize;
            this.damage = damage;
            this.nbtData = nbtData;
        }
    }

    /**
     * Save the current storages to a JSON file
     */
    public void saveStorages() {
        try {
            // Create config directory if it doesn't exist
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            // Convert Storage objects to SerializableStorage objects
            List<SerializableStorage> serializableStorages = new ArrayList<SerializableStorage>();
            for (Storage storage : storages) {
                SerializableItem[] serializableItems = new SerializableItem[storage.contents.length];

                for (int i = 0; i < storage.contents.length; i++) {
                    Slot slot = storage.contents[i];
                    if (slot != null && slot.getStack() != null) {
                        ItemStack stack = slot.getStack();
                        String itemName = stack.getItem().getRegistryName();
                        int stackSize = stack.stackSize;
                        int damage = stack.getItemDamage();
                        String nbtData = "";

                        if (stack.getTagCompound() != null) {
                            nbtData = stack.getTagCompound().toString();
                        }

                        serializableItems[i] = new SerializableItem(itemName, stackSize, damage, nbtData);
                    } else {
                        serializableItems[i] = null; // Empty slot
                    }
                }

                serializableStorages.add(new SerializableStorage(storage.IsEnderChest, storage.StorageNum, serializableItems));
            }

            // Convert to JSON and save
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(serializableStorages);

            FileWriter writer = new FileWriter(STORAGE_FILE_PATH);
            writer.write(json);
            writer.close();

            System.out.println("Storage data saved to " + STORAGE_FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error saving storage data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load storages from a JSON file
     */
    public void loadStorages() {
        try {
            File file = new File(STORAGE_FILE_PATH);
            if (!file.exists()) {
                System.out.println("No storage file found at " + STORAGE_FILE_PATH);
                return;
            }

            // Read JSON from file
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            String json = jsonBuilder.toString();

            // Parse JSON
            Gson gson = new Gson();
            Type listType = new TypeToken<List<SerializableStorage>>(){}.getType();
            List<SerializableStorage> serializableStorages = gson.fromJson(json, listType);

            if (serializableStorages != null) {
                // Note: We can't fully restore Slot objects from serialized data
                // as they contain references to inventories and other complex objects.
                // This method primarily loads the metadata for reference.
                // The actual slot restoration would need to happen when the GUI is opened.

                System.out.println("Loaded " + serializableStorages.size() + " storage entries from " + STORAGE_FILE_PATH);

                // You could store the serializable data for later use when GUIs are opened
                // For now, we'll just print the loaded data
                for (SerializableStorage storage : serializableStorages) {
                    System.out.println("Storage " + storage.StorageNum + " (EnderChest: " + storage.IsEnderChest + ") with " + storage.contents.length + " slots");
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading storage data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error parsing storage data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize the storage system - call this when your mod starts
     */
    public void initialize() {
        loadStorages();
    }


}
