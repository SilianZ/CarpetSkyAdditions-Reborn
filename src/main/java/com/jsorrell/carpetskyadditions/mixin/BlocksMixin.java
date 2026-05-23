package com.jsorrell.carpetskyadditions.mixin;

import com.jsorrell.carpetskyadditions.helpers.CoralSpreader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Function;

@Mixin(Blocks.class)
public abstract class BlocksMixin {

    @Unique
    private static CoralSpreader.CustomCalciteBlock registerCalcite(ResourceKey<Block> Silian_resourceKey, Function<BlockBehaviour.Properties, CoralSpreader.CustomCalciteBlock> Silian_function, BlockBehaviour.Properties Silian_properties) {
        CoralSpreader.CustomCalciteBlock Silian_block = Silian_function.apply(Silian_properties.setId(Silian_resourceKey));
        return  Registry.register(BuiltInRegistries.BLOCK, Silian_resourceKey, Silian_block);
    }

    @Unique
    private static CoralSpreader.CustomCalciteBlock registerCalcite(String Silian_string, Function<BlockBehaviour.Properties, CoralSpreader.CustomCalciteBlock> Silian_function, BlockBehaviour.Properties Silian_properties) {
        return registerCalcite(vanillaBlockId(Silian_string), Silian_function, Silian_properties);
    }

    @Unique
    private static Block registerCalcite(String Silian_string, BlockBehaviour.Properties Silian_properties) {
        return registerCalcite(Silian_string, CoralSpreader.CustomCalciteBlock::new, Silian_properties);
    }

    @Unique
    private static ResourceKey<Block> vanillaBlockId(String Silian_string) {
        return ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(Silian_string));
    }

    @Redirect(
        method = "<clinit>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;register(Ljava/lang/String;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;", ordinal = 0),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=calcite"))
    )
    private static Block registerCustomCalcite(String Silian_name, BlockBehaviour.Properties Silian_properties) {
            return registerCalcite(Silian_name, Silian_properties);
    }

}
