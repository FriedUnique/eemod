package net.friedunique.eemod.common.items;

import net.friedunique.eemod.EEMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EEMod.MOD_ID);

    public static final RegistryObject<Item> WIRE = ITEMS.register("wire_item",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MULTIMETER = ITEMS.register("multimeter_item",
            () -> new MultimeterItem(new MultimeterItem.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
