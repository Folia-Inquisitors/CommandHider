# CommandHider

CommandHider controls which commands players can see, tab-complete, and execute on Paper/Folia servers.

## Folia

This build targets Folia API `1.21.11-R0.1-SNAPSHOT` and declares `folia-supported: true`.

## Replacing CommandWhitelist

To replace CommandWhitelist, remove the old CommandWhitelist jar and install the CommandHider jar. On first start, CommandHider will copy `plugins/CommandWhitelist/config.yml` into `plugins/CommandHider/config.yml` if the new config does not exist yet.

CommandWhitelist-style config keys are accepted:

```yml
unknown-cmd-msg: Unknown command. Type "/help" for help.
groups:
  default:
    hide-all:
      - msg
    add-all:
      - plugins
```

CommandHider also supports its native config shape:

```yml
messages:
  no-permission: '&cUnknown command. Use /help for list of commands.'
command-groups:
  default:
    whitelist:
      - msg
    blacklist:
      - plugins
```

Legacy permissions are accepted alongside CommandHider permissions:

- `commandwhitelist.bypass`
- `commandwhitelist.cwreload`
- `commandwhitelist.<group>`

Native permissions:

- `commandhider.bypass`
- `commandhider.reload`
- `commandhider.<group>`
