package net.friedunique.eemod.common.blocks;

import net.friedunique.eemod.EEMod;
import net.friedunique.eemod.common.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EEMod.MOD_ID);

    public static final RegistryObject<Block> WIRE_BLOCK = registerBlock("wire_block",
            () -> new Wire(BlockBehaviour.Properties.ofFullCopy(Blocks.TRIPWIRE).noCollission().noOcclusion()));


    public static final RegistryObject<Block> RESISTOR_BLOCK = registerBlock("resistor_block",
            () -> new Resistor(BlockBehaviour.Properties.ofFullCopy(Blocks.TRIPWIRE).noCollission().noOcclusion(), 10d));



    public static final RegistryObject<Block> IDEAL_VOLTAGE_SOURCE = registerBlock("ideal_voltage_source",
            () -> new IdealVoltageSource(BlockBehaviour.Properties.of().noCollission().noOcclusion()));

    public static final RegistryObject<Block> IDEAL_CURRENT_SOURCE = registerBlock("ideal_current_source",
            () -> new IdealCurrentSource(BlockBehaviour.Properties.of().noCollission().noOcclusion()));


    public static final RegistryObject<Block> FILAMENT_LAMP_BLOCK = registerBlock("filament_lamp_block",
            () -> new FilamentLamp(BlockBehaviour.Properties.of()));



    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
