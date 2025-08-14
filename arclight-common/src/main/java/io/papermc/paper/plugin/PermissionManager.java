package io.papermc.paper.plugin;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A permission manager implementation to keep backwards compatibility partially alive with existing plugins that used
 * the bukkit one before.
 */
@ApiStatus.Experimental
public interface PermissionManager {

    /**
     * Gets a {@link Permission} from its fully qualified name
     *
     * @param name Name of the permission
     * @return Permission, or null if none
     */
    @Nullable
    Permission getPermission(@NotNull String name);

    /**
     * Adds a {@link Permission} to this plugin manager.
     * <p>
     * If a permission is already defined with the given name of the new
     * permission, an exception will be thrown.
     *
     * @param perm Permission to add
     * @throws IllegalArgumentException Thrown when a permission with the same
     *                                  name already exists
     */
    void addPermission(@NotNull Permission perm);

    /**
     * Removes a {@link Permission} registration from this plugin manager.
     * <p>
     * If the specified permission does not exist in this plugin manager,
     * nothing will happen.
     * <p>
     * Removing a permission registration will <b>not</b> remove the
     * permission from any {@link Permissible}s that have it.
     *
     * @param perm Permission to remove
     */
    void removePermission(@NotNull Permission perm);

    /**
     * Removes a {@link Permission} registration from this plugin manager.
     * <p>
     * If the specified permission does not exist in this plugin manager,
     * nothing will happen.
     * <p>
     * Removing a permission registration will <b>not</b> remove the
     * permission from any {@link Permissible}s that have it.
     *
     * @param name Permission to remove
     */
    void removePermission(@NotNull String name);

    /**
     * Gets the default permissions for the given op status
     *
     * @param op Which set of default permissions to get
     * @return The default permissions
     */
    @NotNull
    Set<Permission> getDefaultPermissions(boolean op);

    /**
     * Recalculates the defaults for the given {@link Permission}.
     * <p>
     * This will have no effect if the specified permission is not registered
     * here.
     *
     * @param perm Permission to recalculate
     */
    void recalculatePermissionDefaults(@NotNull Permission perm);

    /**
     * Subscribes the given Permissible for information about the requested
     * Permission, by name.
     * <p>
     * If the specified Permission changes in any form, the Permissible will
     * be asked to recalculate.
     *
     * @param permission  Permission to subscribe to
     * @param permissible Permissible subscribing
     */
    void subscribeToPermission(@NotNull String permission, @NotNull Permissible permissible);

    /**
     * Unsubscribes the given Permissible for information about the requested
     * Permission, by name.
     *
     * @param permission  Permission to unsubscribe from
     * @param permissible Permissible subscribing
     */
    void unsubscribeFromPermission(@NotNull String permission, @NotNull Permissible permissible);

    /**
     * Gets a set containing all subscribed {@link Permissible}s to the given
     * permission, by name
     *
     * @param permission Permission to query for
     * @return Set containing all subscribed permissions
     */
    @NotNull
    Set<Permissible> getPermissionSubscriptions(@NotNull String permission);

    /**
     * Subscribes to the given Default permissions by operator status
     * <p>
     * If the specified defaults change in any form, the Permissible will be
     * asked to recalculate.
     *
     * @param op          Default list to subscribe to
     * @param permissible Permissible subscribing
     */
    void subscribeToDefaultPerms(boolean op, @NotNull Permissible permissible);

    /**
     * Unsubscribes from the given Default permissions by operator status
     *
     * @param op          Default list to unsubscribe from
     * @param permissible Permissible subscribing
     */
    void unsubscribeFromDefaultPerms(boolean op, @NotNull Permissible permissible);

    /**
     * Gets a set containing all subscribed {@link Permissible}s to the given
     * default list, by op status
     *
     * @param op Default list to query for
     * @return Set containing all subscribed permissions
     */
    @NotNull
    Set<Permissible> getDefaultPermSubscriptions(boolean op);

    /**
     * Gets a copy of all {@link Permission}s registered to this manager
     *
     * @return Copy of all permissions
     */
    @NotNull
    Set<Permission> getPermissions();

    /**
     * Clears the current registered permissibles.
     * This is used for reloading.
     *
     * @return the registered permissibles before clearing
     */
    @NotNull
    Set<Permissible> clearPermissions();

}
