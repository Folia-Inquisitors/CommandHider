# CommandHider

CommandHider controls which commands players can see, tab-complete, and execute on Paper/Folia servers.

## Folia

This build targets Folia API `1.21.11-R0.1-SNAPSHOT` and declares `folia-supported: true`.

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


Native permissions:

- `commandhider.bypass`
- `commandhider.reload`
- `commandhider.<group>`
