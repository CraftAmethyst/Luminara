package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigSerializable
public class  CompatSpec {

    @Setting("material-property-overrides")
    private Map<String, MaterialPropertySpec> materials;

    @Setting("entity-property-overrides")
    private Map<String, EntityPropertySpec> entities;

    @Setting("symlink-world")
    private boolean symlinkWorld;

    @Setting("extra-logic-worlds")
    private List<String> extraLogicWorlds;

    @Setting("permission-forwarding")
    private PermissionForwarding permissionForwarding;

    @Setting("valid-username-regex")
    private String validUsernameRegex;

    @Setting("isolate-plugin-class-loaders")
    private List<String> isolatePluginClassLoaders;

    @Setting("isolate-adventure-from-modloader")
    private boolean isolateAdventureFromModloader;

    @Setting("preload-bungee-chat-classes")
    private boolean preloadBungeeChatClasses = true;

    public Map<String, MaterialPropertySpec> getMaterials() {
        return materials;
    }

    public Optional<MaterialPropertySpec> getMaterial(String key) {
        return Optional.ofNullable(materials.get(key));
    }

    public Map<String, EntityPropertySpec> getEntities() {
        return entities;
    }

    public Optional<EntityPropertySpec> getEntity(String key) {
        return Optional.ofNullable(entities.get(key));
    }

    public boolean isSymlinkWorld() {
        return symlinkWorld;
    }

    public List<String> getExtraLogicWorlds() {
        return extraLogicWorlds;
    }

    public boolean isForwardPermission() {
        return forwarding() == PermissionForwarding.FORGE_TO_BUKKIT;
    }

    public boolean isForwardPermissionReverse() {
        return forwarding() == PermissionForwarding.BUKKIT_TO_FORGE;
    }

    private PermissionForwarding forwarding() {
        return permissionForwarding == null ? PermissionForwarding.DISABLED : permissionForwarding;
    }

    public String getValidUsernameRegex() {
        return validUsernameRegex;
    }

    public boolean isIsolatedPluginClassLoaders(String name) {
        for (String prefix : isolatePluginClassLoaders) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdventureIsolatedFromML() {
        return isolateAdventureFromModloader;
    }

    public boolean isPreloadBungeeChatClasses() {
        return preloadBungeeChatClasses;
    }
}
