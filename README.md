# CommandHider 
## Description
A plugin that controls which commands players can, and cannot see. Supports both Whitelist, blacklist and different permission groups. 

## CommandHider config:

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


## permissions:

- `commandhider.bypass`
- `commandhider.reload`
- `commandhider.<group>`
