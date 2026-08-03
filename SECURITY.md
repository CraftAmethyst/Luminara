# Security Policy

## Supported line

Security fixes target the current `stable/Trials` branch and the supported runtime matrix: Luminara `1.0.14`, Minecraft `1.20.1`, Forge `47.4.22`, and Java 17. Older commits, other Minecraft or Forge versions, and Fabric or NeoForge configurations are unsupported.

## Report a vulnerability

Do not open a public issue for an undisclosed vulnerability. Submit a private report through [GitHub Security Advisories](https://github.com/CraftAmethyst/Luminara/security/advisories/new).

Include:

- the affected commit and Luminara version;
- Minecraft, Forge, and Java versions;
- security impact and required attacker capabilities;
- minimal reproduction steps or a proof of concept;
- relevant logs or stack traces with secrets and personal data removed;
- whether the issue also reproduces on upstream [Arclight](https://github.com/IzzelAliz/Arclight) or Forge.

Do not include live credentials, access tokens, private server data, or third-party personal information. Allow maintainers time to reproduce and coordinate a fix before public disclosure.

## Scope

Luminara-specific launcher, installer, Bukkit bridge, remapper, Mixin, configuration, and server behavior are in scope. Vulnerabilities that reproduce unchanged in Minecraft, Forge, a mod, a plugin, Java, or an upstream library should also be reported to that project through its security process.

The repository does not publish release binaries. CI verification artifacts expire after seven days and are not supported release channels.
