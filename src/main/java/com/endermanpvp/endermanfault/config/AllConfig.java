package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.Dimension;
import com.endermanpvp.endermanfault.DataType.Toggle;
import java.util.HashMap;
import java.util.Map;

import static java.lang.reflect.Array.setBoolean;

public class AllConfig {
    public static AllConfig INSTANCE = new AllConfig();
    private AllConfig()
    {
        BooleanConfig.put("toggle_superpair", new Toggle(true));
        BooleanConfig.put("toggle_armorstandoptimization", new Toggle(true));
        BooleanConfig.put("toggle_enchantbook", new Toggle(true));
        BooleanConfig.put("toggle_storage", new Toggle(true));
        BooleanConfig.put("toggle_storageInInventory", new Toggle(true));
        BooleanConfig.put("toggle_equipmentInInventory", new Toggle(true));
        BooleanConfig.put("toggle_fumo", new Toggle(true));
        DimensionConfig.put("dim_fumo", new Dimension(200,200,1));

    }
    public final HashMap<String,Toggle> BooleanConfig = new HashMap<String,Toggle>();

    public final HashMap<String, Dimension> DimensionConfig = new HashMap<String, Dimension>();

    public void SaveToProperty()
    {
        ModConfig config = ModConfig.instance;
        for(Map.Entry<String,Toggle> entry: BooleanConfig.entrySet())
        {
            config.setBoolean(entry.getKey(), entry.getValue().data);
        }
        for(Map.Entry<String,Dimension> entry: DimensionConfig.entrySet())
        {
            config.setDimension(entry.getKey(), entry.getValue());
        }
    }
    public void LoadFromProperty()
    {
        System.err.println("[AllConfig] Loaded from property");

        if (ModConfig.instance == null) {
            System.err.println("[AllConfig] ModConfig.instance is null, skipping LoadFromProperty");
            return;
        }

        for(Map.Entry<String,Toggle> entry: BooleanConfig.entrySet())
        {
            entry.setValue(new Toggle(ModConfig.instance.getBoolean(entry.getKey(), entry.getValue().data)));
        }
        for(Map.Entry<String,Dimension> entry: DimensionConfig.entrySet())
        {
            Dimension loadedDimension = ModConfig.instance.getDimension(entry.getKey());
            if (loadedDimension != null) {
                entry.setValue(loadedDimension);
            } else {
                System.err.println("[AllConfig] Failed to load dimension for key: " + entry.getKey() + ", keeping default");
            }
        }
    }
}
