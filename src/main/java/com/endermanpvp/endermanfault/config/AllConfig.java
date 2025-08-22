package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.Dimension;
import com.endermanpvp.endermanfault.DataType.MyDataType;
import com.endermanpvp.endermanfault.DataType.MyInt;
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
        BooleanConfig.put("toggle_pingshift", new Toggle(true));

        DimensionConfig.put("dim_fumo", new Dimension(200,200,1));
        IntConfig.put("inp_pingshift_extraping", new MyDataType<>(1));
        StringConfig.put("inp_pingshift_block", new MyDataType<>("minecraft:wool"));
        IntConfig.put("inp_pingshift_blockmeta", new MyDataType<>(14));


    }
    public final HashMap<String,Toggle> BooleanConfig = new HashMap<String,Toggle>();

    public final HashMap<String, MyDataType<String>> StringConfig = new HashMap<String,MyDataType<String>>();
    public final HashMap<String, MyDataType<Integer>> IntConfig = new HashMap<String,MyDataType<Integer>>();

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
        for(Map.Entry<String,MyDataType<Integer>> entry: IntConfig.entrySet())
        {
            config.setString(entry.getKey(), Integer.toString(entry.getValue().data));
        }
        for(Map.Entry<String,MyDataType<String>> entry: StringConfig.entrySet())
        {
            config.setString(entry.getKey(), entry.getValue().data);
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
        for(Map.Entry<String,MyDataType<Integer>> entry: IntConfig.entrySet())
        {
            String loadedValue = ModConfig.instance.getString(entry.getKey(), Integer.toString(entry.getValue().data));
            if (loadedValue != null) {
                entry.getValue().data = Integer.parseInt(loadedValue);
            } else {
                System.err.println("[AllConfig] Failed to load input for key: " + entry.getKey() + ", keeping default");
            }
        }
        for(Map.Entry<String,MyDataType<String>> entry: StringConfig.entrySet())
        {
            String loadedValue = ModConfig.instance.getString(entry.getKey(), entry.getValue().data);
            if (loadedValue != null) {
                entry.getValue().data = loadedValue;
            } else {
                System.err.println("[AllConfig] Failed to load input for key: " + entry.getKey() + ", keeping default");
            }
        }
    }
}
