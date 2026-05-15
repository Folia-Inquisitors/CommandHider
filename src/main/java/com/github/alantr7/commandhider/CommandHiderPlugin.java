package com.github.alantr7.commandhider;

import com.github.alantr7.commandhider.group.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CommandHiderPlugin extends JavaPlugin {

    private static CommandHiderPlugin instance;

    private final GroupManager groupManager = new GroupManager();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new CommandHider(), this);
        CommandHiderAdminCommand adminCommand = new CommandHiderAdminCommand(this);
        registerCommand("commandhider", adminCommand);
        registerCommand("commandwhitelist", adminCommand);

        migrateLegacyConfig();
        saveDefaultConfig();
        groupManager.loadConfig();
    }

    public static CommandHiderPlugin getInstance() {
        return instance;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public boolean hasBypass(Player player) {
        return player.hasPermission("commandhider.bypass")
                || player.hasPermission("commandwhitelist.bypass")
                || player.hasPermission("commandWhitelist.bypass");
    }

    public boolean hasReloadPermission(CommandSender sender) {
        return sender.hasPermission("commandhider.reload")
                || sender.hasPermission("commandwhitelist.cwreload")
                || sender.hasPermission("commandWhitelist.cwreload");
    }

    public String getNoPermissionMessage() {
        return getConfig().getString(
                "messages.no-permission",
                getConfig().getString(
                        "unknown-cmd-msg",
                        "&cUnknown command. Use /help for list of commands."
                )
        );
    }

    private void registerCommand(String commandName, CommandHiderAdminCommand executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null)
            return;

        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void migrateLegacyConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists())
            return;

        File pluginsFolder = getDataFolder().getParentFile();
        if (pluginsFolder == null)
            return;

        File legacyConfig = new File(new File(pluginsFolder, "CommandWhitelist"), "config.yml");
        if (!legacyConfig.isFile())
            return;

        try {
            Files.createDirectories(getDataFolder().toPath());
            Files.copy(legacyConfig.toPath(), configFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            getLogger().info("Copied legacy CommandWhitelist config.yml into the CommandHider data folder.");
        } catch (IOException exception) {
            getLogger().warning("Could not copy legacy CommandWhitelist config.yml: " + exception.getMessage());
        }
    }

}
