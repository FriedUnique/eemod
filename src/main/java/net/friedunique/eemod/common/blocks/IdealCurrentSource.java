package net.friedunique.eemod.common.blocks;

import net.friedunique.eemod.core.Components.*;
import net.friedunique.eemod.core.ElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;

public class IdealCurrentSource extends ElectricalBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    public IdealCurrentSource(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }



    @Override
    public NodeDefinition getNodeDefinition(Level level, BlockPos pos) {
        return new NodeDefinition(ComponentType.SOURCE, SourceType.CURRENT, "1A Source", 1000000, 0, 1);
    }


    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }


    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
    }





    @Override
    public boolean canConnectTo(BlockState state) {
        return false;
    }

    @Override
    public void updateCosmetics(BlockState state, BlockPos pos, BlockPos neighborPos, Level level, boolean isConnectable) {

    }




    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // confusingly, 'getOpposite' makes the face point AT the player.
        // using 'context.getHorizontalDirection()' makes the face point AWAY (like a furnace).
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // 4. Register the Property so the game knows it exists
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
