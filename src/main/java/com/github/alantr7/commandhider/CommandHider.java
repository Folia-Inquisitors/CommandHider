package com.github.alantr7.commandhider;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.github.alantr7.commandhider.group.Group;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandHider implements Listener {

    private static final Set<String> ADMIN_COMMANDS = Set.of("commandhider", "commandwhitelist", "cwreload", "ch");

    @EventHandler
    public void onBrigadierCommandSend(AsyncPlayerSendCommandsEvent<?> event) {
        if (CommandHiderPlugin.getInstance().hasBypass(event.getPlayer()))
            return;

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(event.getPlayer());
        Iterator<? extends CommandNode<?>> children = event.getCommandNode().getChildren().iterator();
        while (children.hasNext()) {
            CommandNode<?> child = children.next();
            String commandName = normalizeRootToken(child.getName());
            if (canUseAdminCommand(event.getPlayer(), commandName))
                continue;

            if (!group.getWhitelist().contains(commandName)) {
                children.remove();
            } else {
                recursivelyCheckBlacklist(commandName, child, group.getBlacklist());
            }
        }
    }

    private void recursivelyCheckBlacklist(String name, CommandNode<?> parent, Set<String> blacklist) {
        if (parent.getChildren().isEmpty())
            return;

        Iterator<? extends CommandNode<?>> children = parent.getChildren().iterator();
        while (children.hasNext()) {
            CommandNode<?> node = children.next();
            if (node instanceof LiteralCommandNode<?> literal) {
                String fullName = name + " " + normalizeToken(literal.getLiteral());
                if (isBlacklisted(fullName, blacklist)) {
                    children.remove();
                } else {
                    recursivelyCheckBlacklist(fullName, literal, blacklist);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSendSuggestions(AsyncPlayerSendSuggestionsEvent event) {
        Player player = event.getPlayer();
        if (CommandHiderPlugin.getInstance().hasBypass(player))
            return;

        String buffer = event.getBuffer();
        if (!buffer.startsWith("/"))
            return;

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(player);
        Suggestions suggestions = event.getSuggestions();
        List<Suggestion> filteredSuggestions = new ArrayList<>();

        for (Suggestion suggestion : suggestions.getList()) {
            String candidate = buildSuggestionCandidate(buffer, suggestion.getText());
            if (!isBlocked(candidate, group) || canUseAdminCommand(player, getRootCommand(candidate)))
                filteredSuggestions.add(suggestion);
        }

        if (filteredSuggestions.size() != suggestions.getList().size()) {
            event.setSuggestions(new Suggestions(suggestions.getRange(), filteredSuggestions));
        }
    }

    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        if (CommandHiderPlugin.getInstance().hasBypass(event.getPlayer()))
            return;

        Group group = CommandHiderPlugin.getInstance().getGroupManager().getGroup(event.getPlayer());
        String command = normalizeCommand(event.getMessage());
        if (canUseAdminCommand(event.getPlayer(), getRootCommand(command)))
            return;

        if (isBlocked(command, group)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    CommandHiderPlugin.getInstance().getNoPermissionMessage()
            ));
        }
    }

    private String buildSuggestionCandidate(String buffer, String suggestion) {
        String command = stripLeadingSlash(buffer);
        String suggestionText = stripLeadingSlash(suggestion);

        if (command.endsWith(" ")) {
            return normalizeCommand(command + suggestionText);
        }

        int lastSpace = command.lastIndexOf(' ');
        if (lastSpace < 0) {
            return normalizeCommand(suggestionText);
        }

        return normalizeCommand(command.substring(0, lastSpace + 1) + suggestionText);
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
        String command = stripLeadingSlash(commandLine).trim().toLowerCase(Locale.ROOT);
        if (command.isEmpty())
            return "";

        String[] parts = command.split("\\s+");
        parts[0] = stripNamespace(parts[0]);
        return String.join(" ", parts);
    }

    private String normalizeRootToken(String token) {
        return stripNamespace(normalizeToken(token));
    }

    private String normalizeToken(String token) {
        return token.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
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

}
