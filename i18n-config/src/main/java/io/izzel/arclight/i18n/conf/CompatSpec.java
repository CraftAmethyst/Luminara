package io.izzel.arclight.i18n.conf;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CompatSpec {

    @Setting("preload-bungee-chat-classes")
    private final boolean preloadBungeeChatClasses = true;

    @Setting("material-property-overrides")
    private Map<String, MaterialPropertySpec> materials = Map.of();

    @Setting("entity-property-overrides")
    private Map<String, EntityPropertySpec> entities = Map.of();

    @Setting("symlink-world")
    private boolean symlinkWorld;

    @Setting("extra-logic-worlds")
    private List<String> extraLogicWorlds = List.of();

    @Setting("permission-forwarding")
    private PermissionForwarding permissionForwarding =
        PermissionForwarding.DISABLED;

    @Setting("valid-username-regex")
    private String validUsernameRegex = "";

    @Setting("lenient-item-tag-match")
    private boolean lenientItemTagMatch;

    @Setting("enable-bukkit-reload-command")
    private final boolean enableBukkitReloadCommand = true;

    public Map<String, MaterialPropertySpec> getMaterials() {
        return materials == null ? Map.of() : materials;
    }

    public Optional<MaterialPropertySpec> getMaterial(String key) {
        return Optional.ofNullable(getMaterials().get(key));
    }

    public Map<String, EntityPropertySpec> getEntities() {
        return entities == null ? Map.of() : entities;
    }

    public Optional<EntityPropertySpec> getEntity(String key) {
        return Optional.ofNullable(getEntities().get(key));
    }

    public boolean isSymlinkWorld() {
        return symlinkWorld;
    }

    public List<String> getExtraLogicWorlds() {
        return extraLogicWorlds == null ? List.of() : extraLogicWorlds;
    }

    public PermissionForwarding getPermissionForwarding() {
        return permissionForwarding == null
            ? PermissionForwarding.DISABLED
            : permissionForwarding;
    }

    public String getValidUsernameRegex() {
        return validUsernameRegex == null ? "" : validUsernameRegex;
    }

    public boolean isLenientItemTagMatch() {
        return lenientItemTagMatch;
    }

    public boolean isPreloadBungeeChatClasses() {
        return preloadBungeeChatClasses;
    }

    public boolean isEnableBukkitReloadCommand() {
        return enableBukkitReloadCommand;
    }
}
