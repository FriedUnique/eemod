package net.friedunique.eemod.common.entities;

import net.friedunique.eemod.EEMod;
import net.friedunique.eemod.common.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EEMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<FilamentBlockEntity>> LAMP_BE =
            BLOCK_ENTITIES.register("lamp_block_entity", () -> BlockEntityType.Builder.of(FilamentBlockEntity::new, ModBlocks.FILAMENT_LAMP_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
