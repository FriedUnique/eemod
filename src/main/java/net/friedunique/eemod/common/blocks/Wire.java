package net.friedunique.eemod.common.blocks;

import net.friedunique.eemod.common.ModTags;
import net.friedunique.eemod.core.Components.ComponentType;
import net.friedunique.eemod.core.ElectricalBlock;

import net.friedunique.eemod.core.network.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


public class Wire extends ElectricalBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;


    public Wire(Properties properties){
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false));
    }



    @Override
    public NodeDefinition getNodeDefinition(Level level, BlockPos pos) {
        boolean[] isTouching = checkNeighborTerminals(level, pos);
        // HAS TO BE NON-ZERO!!!
        return new NodeDefinition(ComponentType.CONDUCTOR, "", 0.000000001, isTouching[0], isTouching[1]);
    }



    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }











    // ---- cosmetics ----
    @Override
    public void updateCosmetics(BlockState state, BlockPos pos, BlockPos neighborPos, boolean isConnectable) {
        int dx = neighborPos.getX()-pos.getX();
        int dy = neighborPos.getY()-pos.getY();
        int dz = neighborPos.getZ()-pos.getZ();

        System.out.println("Cosmetics update because neightbor was placed");

        state.setValue(getPropForDir(Direction.fromDelta(dx, dy, dz)), isConnectable);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getPropForDir(direction), this.canConnectTo(neighborState));
    }

    @Override
    public boolean canConnectTo(BlockState state) {
        //visual
        return state.is(ModTags.CONDUCTIVE_BLOCKS);
    }

    private BooleanProperty getPropForDir(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            default -> WEST;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST);
    }


    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Player walks right through it
    }

}
