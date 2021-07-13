package org.acornmc.piglinnames;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class CommandPrefix implements CommandExecutor {
    ConfigManager configManager;

    public CommandPrefix(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean validatePerms(CommandSender sender, List<String> permissions) {
        for (String perm : permissions) {
            if (perm.startsWith("!")) {
                if (sender.hasPermission(perm.substring(1))) {
                    return false;
                }
            } else {
                if (!sender.hasPermission(perm)) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        ConfigurationSection cs = configManager.get().getConfigurationSection("prefix");
        for (String key : cs.getKeys(false)) {
            boolean hasAllPerms = validatePerms(sender, configManager.get().getStringList("prefix." + key + ".permissions"));
            if (hasAllPerms) {

                //Check regex
                String regex = configManager.get().getString("prefix." + key + ".regex");
                if (args[0].matches(regex)) {

                    // Execute commands
                    List<String> commands = configManager.get().getStringList("prefix." + key + ".commands");
                    for (String c : commands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("<player>", sender.getName()).replace("<argument>", args[0]));
                    }
                    sender.sendMessage("§cYour prefix color has been changed");
                    return true;
                }
            }
        }

        sender.sendMessage("§cPermission & regex combination not found");
        return true;
    }
}
