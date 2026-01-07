package net.friedunique.eemod.common.items;

import net.friedunique.eemod.core.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MultimeterItem extends Item {

    public MultimeterItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos();
            Player player = context.getPlayer();

            // Ask the Network Manager for data at this position
            double voltage = NetworkManager.get(level).getVoltageAt(pos);
            double current = NetworkManager.get(level).getCurrentAt(pos);

            if (player != null) {
                String message = String.format("§e--- Circuit Data ---§r\n" +
                                "Voltage: §b%.2f V§r\n" +
                                "Current: §6%.2f A§r",
                        voltage, current);
                player.sendSystemMessage(Component.literal(message));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
