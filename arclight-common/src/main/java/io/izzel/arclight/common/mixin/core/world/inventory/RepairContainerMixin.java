package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.entity.player.PlayerEntityBridge;
import io.izzel.arclight.common.bridge.core.util.IWorldPosCallableBridge;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftInventory;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class RepairContainerMixin extends ItemCombinerMixin {

    // @formatter:off
    @Shadow @Final public DataSlot cost;
    @Shadow public int repairItemCountCost;
    @Shadow public String itemName;
    public int cancelThisBySettingCostToMaximum = 40;
    // @formatter:on
    public int maximumRenameCostThreshold = 40;
    public int maximumAllowedRenameCost = 39;
    public int maximumRepairCost = 40;
    private CraftInventoryView bukkitEntity;

    @Shadow
    public static int calculateIncreasedRepairCost(int oldRepairCost) {
        return 0;
    }

    // Below overwrite is removed to support injecting into createResult()
    // See #1636
    /*
     * @author IzzelAliz
     * @reason
     */
    //@Overwrite
    //public void createResult()

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void arclight$callInventoryEvent(ResultContainer instance, int slot, ItemStack itemStack) {
        //The slot id is useless for DataSlot
        CraftEventFactory.callPrepareAnvilEvent(getBukkitView(), itemStack);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    private void arclight$sendData(CallbackInfo ci) {
        sendAllDataToRemote();
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40), require = 1)
    private int arclight$maximumRepairCost(int raw) {
        return raw - 40 + maximumRepairCost;
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 39), require = 1)
    private int arclight$maximumRenameCost(int raw) {
        return raw - 40 + maximumRepairCost;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventory inventory = new CraftInventoryAnvil(
                ((IWorldPosCallableBridge) this.access).bridge$getLocation(), this.inputSlots, this.resultSlots, (AnvilMenu) (Object) this);
        bukkitEntity = new CraftInventoryView(((PlayerEntityBridge) this.player).bridge$getBukkitEntity(), inventory, (AbstractContainerMenu) (Object) this);
        return bukkitEntity;
    }
}
