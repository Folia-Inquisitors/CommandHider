package com.github.alantr7.commandhider.group;

public class Group {

    private final String id;

    private final CommandRuleSet whitelist;

    private final CommandRuleSet blacklist;

    public Group(String id, CommandRuleSet whitelist, CommandRuleSet blacklist) {
        this.id = id;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public String getId() {
        return id;
    }

    public CommandRuleSet getWhitelist() {
        return whitelist;
    }

    public CommandRuleSet getBlacklist() {
        return blacklist;
    }

}
