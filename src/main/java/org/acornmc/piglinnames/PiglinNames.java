package org.acornmc.piglinnames;

import org.bukkit.plugin.java.JavaPlugin;

public final class PiglinNames extends JavaPlugin {

    ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);

        getCommand("prefix").setExecutor(new CommandPrefix(configManager));
        getServer().getPluginManager().registerEvents(new EventLogin(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
