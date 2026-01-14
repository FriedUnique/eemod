package net.friedunique.eemod.common.entities;

import net.friedunique.eemod.common.blocks.FilamentLamp;
import net.friedunique.eemod.core.network.Edge;
import net.friedunique.eemod.core.network.NetworkManager;
import net.friedunique.eemod.core.network.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FilamentBlockEntity extends BlockEntity {
    public FilamentBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.LAMP_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FilamentBlockEntity be) {
        // reduce updating
        if (level.getGameTime() % 3 != 0) return;

        NetworkManager networkManager = NetworkManager.get(level);
        Node node = networkManager.getPosToNode().get(pos);

        if (node != null) {
            double powerUsage = be.getVoltageDropOverLoad(node)*node.getTotalFlow();


            int light = be.mapPowerToLight(powerUsage);

            //add bloom maybe
            if (state.getValue(FilamentLamp.LIGHT_LEVEL) != light) {
                System.out.println();
                System.out.println(be.getVoltageDropOverLoad(node));
                System.out.println("------------->> " + powerUsage);
                level.setBlock(pos, state.setValue(FilamentLamp.LIGHT_LEVEL, light), 2);
            }
        }
    }

    private double getVoltageDropOverLoad(Node node){
        double voltageDrop = 0;

        for(Edge edge : node.connectedEdges){
            if(edge.nodeOrigin == node){
                voltageDrop += edge.simulatedEdgeVoltage;
            }else{
                voltageDrop -= edge.simulatedEdgeVoltage;
            }
        }

        return Math.abs(voltageDrop);
    }

    private int mapPowerToLight(double power) {
        if (power < 5) return 0; // Too dim to glow
        return (int) Math.min(15, (power / 60.0) * 15);
    }
}
