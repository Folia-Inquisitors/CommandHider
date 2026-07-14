package com.github.alantr7.commandhider;

import com.github.alantr7.commandhider.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandHider implements Listener {

    private static final Set<String> ADMIN_COMMANDS = Set.of("commandhider", "commandwhitelist", "cwreload", "ch");

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (CommandHiderPlugin.getInstance().hasBypass(player))
            return;

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(player);
        event.getCommands().removeIf(command -> {
            if (hasNamespace(getRootCommand(normalizeToken(command))))
                return true;

            String rootCommand = normalizeRootToken(command);
            return !canUseAdminCommand(player, rootCommand) && !group.getWhitelist().contains(rootCommand);
        });
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player))
            return;

        if (CommandHiderPlugin.getInstance().hasBypass(player))
            return;

        String buffer = event.getBuffer();
        if (!buffer.startsWith("/"))
            return;

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(player);
        List<String> completions = new ArrayList<>(event.getCompletions());
        completions.removeIf(completion -> {
            String rawCandidate = buildRawSuggestionCandidate(buffer, completion);
            if (hasNamespace(getRootCommand(rawCandidate)))
                return true;

            String candidate = normalizeCommand(rawCandidate);
            return isBlocked(candidate, group) && !canUseAdminCommand(player, getRootCommand(candidate));
        });

        if (completions.size() != event.getCompletions().size())
            event.setCompletions(completions);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        if (CommandHiderPlugin.getInstance().hasBypass(event.getPlayer()))
            return;

        String rawCommand = normalizeRawCommand(event.getMessage());
        if (hasNamespace(getRootCommand(rawCommand))) {
            event.setCancelled(true);
            sendNoPermissionMessage(event.getPlayer());
            return;
        }

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(event.getPlayer());
        String command = normalizeCommand(rawCommand);
        if (canUseAdminCommand(event.getPlayer(), getRootCommand(command)))
            return;

        if (isBlocked(command, group)) {
            event.setCancelled(true);
            sendNoPermissionMessage(event.getPlayer());
        }
    }

    private void sendNoPermissionMessage(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes(
                '&',
                CommandHiderPlugin.getInstance().getNoPermissionMessage()
        ));
    }

    private String buildRawSuggestionCandidate(String buffer, String suggestion) {
        String command = stripLeadingSlash(buffer);
        String suggestionText = stripLeadingSlash(suggestion);

        if (command.endsWith(" ")) {
            return normalizeRawCommand(command + suggestionText);
        }

        int lastSpace = command.lastIndexOf(' ');
        if (lastSpace < 0) {
            return normalizeRawCommand(suggestionText);
        }

        return normalizeRawCommand(command.substring(0, lastSpace + 1) + suggestionText);
    }

    private boolean isBlocked(String command, Group group) {
        if (command.isEmpty())
            return false;

        String root = getRootCommand(command);
        return !group.getWhitelist().contains(root) || isBlacklisted(command, group.getBlacklist());
    }

    private boolean canUseAdminCommand(Player player, String command) {
        return ADMIN_COMMANDS.contains(command) && CommandHiderPlugin.getInstance().hasReloadPermission(player);
    }

    private boolean isBlacklisted(String command, Set<String> blacklist) {
        String[] parts = command.split("\\s+");
        StringBuilder commandPart = new StringBuilder();
        for (String part : parts) {
            if (commandPart.length() > 0)
                commandPart.append(' ');

            commandPart.append(part);
            if (blacklist.contains(commandPart.toString()))
                return true;
        }
        return false;
    }

    private String getRootCommand(String command) {
        int firstSpace = command.indexOf(' ');
        if (firstSpace < 0)
            return command;
        return command.substring(0, firstSpace);
    }

    private String normalizeCommand(String commandLine) {
        String command = normalizeRawCommand(commandLine);
        if (command.isEmpty())
            return "";

        String[] parts = command.split("\\s+");
        parts[0] = stripNamespace(parts[0]);
        return String.join(" ", parts);
    }

    private String normalizeRawCommand(String commandLine) {
        return stripLeadingSlash(commandLine).trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String normalizeRootToken(String token) {
        return stripNamespace(normalizeToken(token));
    }

    private String normalizeToken(String token) {
        return stripLeadingSlash(token).trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String stripLeadingSlash(String command) {
        if (command == null)
            return "";

        return command.startsWith("/") ? command.substring(1) : command;
    }

    private String stripNamespace(String command) {
        int namespaceSeparator = command.indexOf(':');
        if (namespaceSeparator < 0 || namespaceSeparator + 1 >= command.length())
            return command;
        return command.substring(namespaceSeparator + 1);
    }

    private boolean hasNamespace(String command) {
        return command.indexOf(':') >= 0;
    }

}
