package com.github.alantr7.commandhider.group;

import java.util.Set;

public class Group {

    private final String id;

    private final Set<String> whitelist;

    private final Set<String> blacklist;

    public Group(String id, Set<String> whitelist, Set<String> blacklist) {
        this.id = id;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public String getId() {
        return id;
    }

    public Set<String> getWhitelist() {
        return whitelist;
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }

}
