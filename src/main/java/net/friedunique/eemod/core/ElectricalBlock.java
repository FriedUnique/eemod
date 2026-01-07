package net.friedunique.eemod.core;

import net.friedunique.eemod.core.Components.ComponentType;
import net.friedunique.eemod.core.network.Node;
import net.friedunique.eemod.core.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;

public abstract class ElectricalBlock extends Block {
    public ComponentType type;

    public ElectricalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {

            NetworkManager.get(level).addNode(level, pos, this);
        }

    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide) {
            NetworkManager.get(level).removeNode(pos);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    public boolean[] checkNeighborTerminals(Level level, BlockPos pos){
        // postitve, negative

        boolean[] isTouching = new boolean[2];
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            if(level.getBlockState(neighborPos).getBlock() instanceof ElectricalBlock electricalBlock){
                if(electricalBlock.type != ComponentType.SOURCE) { return isTouching; }

                isTouching[0] = touchingFrontFace(level, pos, dir); // positive
                isTouching[1] = touchingBackFace(level, pos, dir); // negative
            }
        }
        return isTouching;
    }

    public abstract NodeDefinition getNodeDefinition(Level level, BlockPos pos);



    public record NodeDefinition(ComponentType type, double resistance, double sourceVoltage, boolean isTouchingPositiveTerminal, boolean isTouchingNegativeTerminal, Node positiveNode, Node negativeNode) {
        public NodeDefinition(ComponentType type, double resistance, double sourceVoltage, boolean isTouchingPositive, boolean isTouchingNegative) {this(type, resistance, sourceVoltage, isTouchingPositive, isTouchingNegative, null, null);}
        public NodeDefinition(ComponentType type, double resistance, double sourceVoltage) {this(type, resistance, sourceVoltage, false, false, null, null);}

    }









    public boolean touchingFrontFace(Level level, BlockPos myPos, Direction dirToNeighbor) {
        BlockPos neighborPos = myPos.relative(dirToNeighbor);
        BlockState neighborState = level.getBlockState(neighborPos);

        // 1. Check if the neighbor actually has a facing property
        if (neighborState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction neighborFacing = neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            return neighborFacing == dirToNeighbor.getOpposite();
        }
        return false; // Not a directional block
    }

    public boolean touchingBackFace(Level level, BlockPos myPos, Direction dirToNeighbor) {
        BlockPos neighborPos = myPos.relative(dirToNeighbor);
        BlockState neighborState = level.getBlockState(neighborPos);

        // 1. Check if the neighbor actually has a facing property
        if (neighborState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction neighborFacing = neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING);

            return neighborFacing == dirToNeighbor;
        }
        return false; // Not a directional block
    }
}
