package com.endermanpvp.endermanfault.config;

import com.endermanpvp.endermanfault.DataType.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.io.*;
import java.util.Properties;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "config/endermanfault.cfg";
    public static ModConfig instance = new ModConfig();
    private final Properties properties;
    private File configFile;

    private ModConfig() {
        properties = new Properties();
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, CONFIG_FILE_NAME);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        this.configFile = configDir;

    }




    public void loadConfig() {

        if (configFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(configFile);
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                // Only load from property if AllConfig is properly initialized
                try {
                    AllConfig.INSTANCE.LoadFromProperty();
                } catch (Exception e) {
                    System.err.println("Failed to load from property during config initialization: " + e.getMessage());
                }
            }
        } else {
            // Config file doesn't exist, just ensure AllConfig is loaded with defaults
            try {
                AllConfig.INSTANCE.LoadFromProperty();
            } catch (Exception e) {
                System.err.println("Failed to load default properties: " + e.getMessage());
            }
        }

    }
    public void saveConfig() {
        AllConfig.INSTANCE.SaveToProperty();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(configFile);
            properties.store(fos, "EndermanFault Mod Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public Dimension getDimension(String MainKey) {
        return new Dimension(
            getInt(MainKey + "_x", 200),
            getInt(MainKey + "_y", 200),
            getFloat(MainKey + "_scale", 1) // Default scale is 1
        );
    }
    public void setDimension(String MainKey, Dimension dimension) {
        setInt(MainKey + "_x", dimension.x);
        setInt(MainKey + "_y", dimension.y);
        setFloat(MainKey + "_scale", dimension.Scale);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            setBoolean(key, defaultValue);
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            setInt(key, defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            setInt(key, defaultValue);
            return defaultValue;
        }
    }
    public void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public String getString(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            setString(key, defaultValue);
            return defaultValue;
        }
        return value;
    }
    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }

    public float getFloat(String key, float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            setFloat(key, defaultValue);
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            setFloat(key, defaultValue);
            return defaultValue;
        }
    }
    public void setFloat(String key, float value) {
        properties.setProperty(key, String.valueOf(value));
    }
}
