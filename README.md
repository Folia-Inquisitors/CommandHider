# CommandHider

CommandHider controls which commands players can see, tab-complete, and execute on Paper/Folia servers.

CommandHider config:

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
