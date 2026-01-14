package net.friedunique.eemod.core;

import net.friedunique.eemod.common.ModTags;
import net.friedunique.eemod.core.Components.SourceType;
import net.friedunique.eemod.core.Components.ComponentType;
import net.friedunique.eemod.core.network.Node;
import net.friedunique.eemod.core.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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


            updateCosmeticConnection(pos, level, state);
        }

        super.onPlace(state, level, pos, oldState, isMoving);

    }


    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide) {
            NetworkManager.get(level).removeNode(pos);
//            updateCosmeticConnection(pos, level, state);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }


    private void updateCosmeticConnection(BlockPos pos, Level level, BlockState state){
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if(neighborState.is(ModTags.CONDUCTIVE_BLOCKS)){
                updateCosmetics(state, pos, neighborPos, level, canConnectTo(neighborState));
            }
        }
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

    public abstract boolean canConnectTo(BlockState state);
    public abstract void updateCosmetics(BlockState state, BlockPos pos, BlockPos neighborPos, Level level, boolean isConnectable);
    public abstract NodeDefinition getNodeDefinition(Level level, BlockPos pos);



    public record NodeDefinition(ComponentType type, SourceType sourceType, String name, double resistance, double sourceVoltage, double sourceCurrent, boolean isTouchingPositiveTerminal, boolean isTouchingNegativeTerminal, double heatCoefficient, double powerUsage, Node positiveNode, Node negativeNode) {
        // wire and resistors
        public NodeDefinition(ComponentType type, String name, double resistance, boolean isTouchingPositive, boolean isTouchingNegative) {this(type, SourceType.NONE, name, resistance, 0d, 0d, isTouchingPositive, isTouchingNegative, 0, 0, null, null);}
        public NodeDefinition(ComponentType type, double resistance, boolean isTouchingPositive, boolean isTouchingNegative) {this(type, SourceType.NONE, "", resistance, 0d, 0d, isTouchingPositive, isTouchingNegative, 0, 0, null, null);}
        //public NodeDefinition(ComponentType type, double resistance, double heatCoefficient, double powerUsage, boolean isTouchingPositive, boolean isTouchingNegative) {this(type, SourceType.NONE, "", resistance, 0d, 0d, isTouchingPositive, isTouchingNegative, heatCoefficient, powerUsage, null, null);}

        // sources
        public NodeDefinition(ComponentType componentType, SourceType sourceType, String name, double resistance, double sourceVoltage, double sourceCurrent) {this(componentType, sourceType, name, resistance, sourceVoltage, sourceCurrent, false, false, 0, 0, null, null);}
        public NodeDefinition(ComponentType componentType, SourceType sourceType, double resistance, double sourceVoltage, double sourceCurrent) {this(componentType, sourceType, "", resistance, sourceVoltage, sourceCurrent, false, false, 0, 0, null, null);}


        // Lamp etc
        public NodeDefinition(ComponentType type, String name, double resistance, double heatCoefficient, double powerUsage, boolean isTouchingPositive, boolean isTouchingNegative) {this(type, SourceType.NONE, name, resistance, 0d, 0d, isTouchingPositive, isTouchingNegative, heatCoefficient, powerUsage, null, null);}


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
