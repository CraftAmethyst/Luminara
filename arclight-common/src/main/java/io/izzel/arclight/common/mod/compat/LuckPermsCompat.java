package io.izzel.arclight.common.mod.compat;

import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.permissions.PermissibleBase;

import java.lang.reflect.Field;

public final class LuckPermsCompat {

    private static final Field HUMAN_ENTITY_PERMISSIBLE_FIELD =
        findField(CraftHumanEntity.class, "perm");
    private static final Field PERMISSIBLE_BASE_ATTACHMENTS_FIELD =
        findField(PermissibleBase.class, "attachments");

    private LuckPermsCompat() {
    }

    public static Field humanEntityPermissibleField() {
        return HUMAN_ENTITY_PERMISSIBLE_FIELD;
    }

    public static Field permissibleBaseAttachmentsField() {
        return PERMISSIBLE_BASE_ATTACHMENTS_FIELD;
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
