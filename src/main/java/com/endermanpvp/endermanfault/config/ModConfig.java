package com.endermanpvp.endermanfault.config;

import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.Properties;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "endermanfault.cfg";
    private static ModConfig instance;
    private final Properties properties;
    private final File configFile;

    private ModConfig() {
        properties = new Properties();
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
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
            }
        }
    }

    public void saveConfig() {
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
        saveConfig();
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
        saveConfig();
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
        saveConfig();
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
        saveConfig();
    }
}
