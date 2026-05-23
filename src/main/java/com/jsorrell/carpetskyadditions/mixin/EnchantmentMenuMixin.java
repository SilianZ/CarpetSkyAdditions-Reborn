package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.settings.SkyAdditionsSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.jsorrell.carpetskyadditions.helpers.SkyAdditionsEnchantmentHelper.MAX_WARDEN_DISTANCE_FOR_SWIFT_SNEAK;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {

    @Shadow
    @Final
    private ContainerLevelAccess access;

    @WrapOperation(
        method = "getEnchantmentList",
        at =
        @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;"
        )
    )
    private List<EnchantmentInstance> addSwiftSneak(
        RandomSource Silian_randomSource, ItemStack Silian_stack,
        int Silian_i, Stream<Holder<Enchantment>> Silian_stream,
        Operation<List<EnchantmentInstance>> Silian_original
    ) {
        if (SkyAdditionsSettings.renewableSwiftSneak) {
            boolean Silian_hasWardenNearby = access.evaluate((Silian_world, Silian_blockPos) -> {
                    AABB Silian_box = new AABB(Silian_blockPos).inflate(MAX_WARDEN_DISTANCE_FOR_SWIFT_SNEAK);
                    Vec3 Silian_tablePos = Vec3.atBottomCenterOf(Silian_blockPos).relative(Direction.UP, 0.375);
                    Predicate<Warden> Silian_rangePredicate =
                        Silian_e -> Silian_e.position().closerThan(Silian_tablePos, MAX_WARDEN_DISTANCE_FOR_SWIFT_SNEAK);
                    List<Warden> Silian_wardenEntities = Silian_world.getEntitiesOfClass(Warden.class, Silian_box, Silian_rangePredicate);
                    return !Silian_wardenEntities.isEmpty();
                })
                .orElseThrow();

            if (Silian_hasWardenNearby) {
                Silian_stack = Silian_stack.copy();
                CustomData Silian_customData = Silian_stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag Silian_tag = Silian_customData.copyTag();
                Silian_tag.putBoolean("SWIFT_SNEAK_ENCHANTABLE", true);
                Silian_stack.set(DataComponents.CUSTOM_DATA, CustomData.of(Silian_tag));
            }
        }
        return Silian_original.call(Silian_randomSource, Silian_stack, Silian_i, Silian_stream);
    }
}
