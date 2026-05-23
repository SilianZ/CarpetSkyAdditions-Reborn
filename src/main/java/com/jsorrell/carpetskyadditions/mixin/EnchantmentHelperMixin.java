package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.SkyAdditionsExtension;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(
        method = "getAvailableEnchantmentResults",
        at =
        @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static void forceAllowSwiftSneak(
        int Silian_i, ItemStack Silian_stack, Stream<Holder<Enchantment>> Silian_stream,
        CallbackInfoReturnable<List<EnchantmentInstance>> Silian_cir, @Local List<EnchantmentInstance> Silian_enchantmentList) {
        if (SkyAdditionsExtension.minecraftServer != null) {
            Holder.Reference<Enchantment> Silian_swiftSneak = SkyAdditionsExtension.minecraftServer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SWIFT_SNEAK).orElseThrow();
            CustomData Silian_customData = Silian_stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (Silian_customData.copyTag().getBoolean("SWIFT_SNEAK_ENCHANTABLE").isPresent() && Silian_customData.copyTag().getBoolean("SWIFT_SNEAK_ENCHANTABLE").get()  /*EnchantmentHelperContexts.FORCE_ALLOW_SWIFT_SNEAK.get()*/) {
                if (Silian_swiftSneak.value().canEnchant(Silian_stack) || Silian_stack.is(Items.BOOK)) {
                    for (int Silian_level = 1; Silian_level <= 3; Silian_level++) {
                        Silian_enchantmentList.add(new EnchantmentInstance(
                            Silian_swiftSneak,
                            Silian_level));
                    }
                }
            }
        }
    }
}
