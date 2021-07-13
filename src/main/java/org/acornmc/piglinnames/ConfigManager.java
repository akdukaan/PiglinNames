package org.acornmc.piglinnames;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {
    Plugin pluginRef;
    FileConfiguration fileConfiguration;

    public ConfigManager(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        pluginRef = plugin;
        fileConfiguration = pluginRef.getConfig();
    }

    public FileConfiguration get() {
        return fileConfiguration;
    }

    public void reload() {
        pluginRef.reloadConfig();
        fileConfiguration = pluginRef.getConfig();
    }
}
