package com.github.alantr7.commandhider.group;

import com.github.alantr7.commandhider.CommandHiderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

public class GroupManager {

    private static final Group EMPTY_DEFAULT_GROUP = new Group("default", Set.of(), Set.of());

    private final Map<String, Group> groups = new LinkedHashMap<>();

    public void loadConfig() {
        groups.clear();

        FileConfiguration config = CommandHiderPlugin.getInstance().getConfig();
        ConfigurationSection commandGroupsSection = config.getConfigurationSection("command-groups");
        if (commandGroupsSection == null) {
            commandGroupsSection = config.getConfigurationSection("groups");
        }

        if (commandGroupsSection == null)
            return;

        for (String groupId : commandGroupsSection.getKeys(false)) {
            ConfigurationSection groupSection = commandGroupsSection.getConfigurationSection(groupId);
            if (groupSection == null)
                continue;

            String normalizedGroupId = normalize(groupId);
            groups.put(normalizedGroupId, new Group(
                    normalizedGroupId,
                    readCommandSet(groupSection, "whitelist", "hide-all"),
                    readCommandSet(groupSection, "blacklist", "add-all")
            ));
            registerGroupPermission(normalizedGroupId);
        }
    }

    public Group getGroup(Player player) {
        for (var entry : groups.entrySet()) {
            if (entry.getKey().equals("default"))
                continue;

            if (hasGroupPermission(player, entry.getKey()))
                return entry.getValue();
        }
        return groups.getOrDefault("default", EMPTY_DEFAULT_GROUP);
    }

    private Set<String> readCommandSet(ConfigurationSection groupSection, String primaryKey, String legacyKey) {
        Set<String> commands = new LinkedHashSet<>();
        commands.addAll(readCommandSet(groupSection, primaryKey));
        commands.addAll(readCommandSet(groupSection, legacyKey));
        return commands;
    }

    private Set<String> readCommandSet(ConfigurationSection groupSection, String key) {
        return groupSection.getStringList(key).stream()
                .map(this::normalizeCommand)
                .filter(command -> !command.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void registerGroupPermission(String groupId) {
        registerPermission("commandhider." + groupId);
        registerPermission("commandwhitelist." + groupId);
        registerPermission("commandWhitelist." + groupId);
    }

    private boolean hasGroupPermission(Player player, String groupId) {
        return player.hasPermission("commandhider." + groupId)
                || player.hasPermission("commandwhitelist." + groupId)
                || player.hasPermission("commandWhitelist." + groupId);
    }

    private void registerPermission(String permissionName) {
        if (Bukkit.getPluginManager().getPermission(permissionName) == null) {
            Bukkit.getPluginManager().addPermission(new Permission(permissionName, PermissionDefault.FALSE));
        }
    }

    private String normalizeCommand(String command) {
        String normalized = normalize(command);
        String[] parts = normalized.split("\\s+");
        if (parts.length == 0)
            return "";

        parts[0] = stripNamespace(parts[0]);
        return String.join(" ", parts);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String stripNamespace(String command) {
        int namespaceSeparator = command.indexOf(':');
        if (namespaceSeparator < 0 || namespaceSeparator + 1 >= command.length())
            return command;
        return command.substring(namespaceSeparator + 1);
    }

}
