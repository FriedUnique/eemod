package net.friedunique.eemod.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {

    public static final TagKey<Block> CONDUCTIVE_BLOCKS = tag("conductive");

    public static TagKey<Block> tag(String name){
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("eemod", name));
    }
}
