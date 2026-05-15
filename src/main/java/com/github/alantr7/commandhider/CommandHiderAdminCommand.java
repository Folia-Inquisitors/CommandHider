package com.github.alantr7.commandhider;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

public class CommandHiderAdminCommand implements CommandExecutor, TabCompleter {

    private final CommandHiderPlugin plugin;

    public CommandHiderAdminCommand(CommandHiderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.hasReloadPermission(sender)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getGroupManager().loadConfig();
            sender.sendMessage(ChatColor.GREEN + "CommandHider configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /commandhider reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.hasReloadPermission(sender) || args.length != 1)
            return List.of();

        return "reload".startsWith(args[0].toLowerCase(Locale.ROOT)) ? List.of("reload") : List.of();
    }

}
