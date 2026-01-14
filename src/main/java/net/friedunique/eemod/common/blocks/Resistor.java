package net.friedunique.eemod.common.blocks;

import net.friedunique.eemod.common.ModTags;
import net.friedunique.eemod.core.Components;
import net.friedunique.eemod.core.ElectricalBlock;
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
import org.jetbrains.annotations.NotNull;

public class Resistor extends ElectricalBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private double resistance;

    public Resistor(Properties properties, double resistance) {
        super(properties);

        this.resistance = 10;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false));
    }

    @Override
    public NodeDefinition getNodeDefinition(Level level, BlockPos pos) {
        boolean[] isTouching = checkNeighborTerminals(level, pos);
        return new NodeDefinition(Components.ComponentType.LOAD, "10 ohm resistance", resistance, isTouching[0], isTouching[1]);
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getPropForDir(direction), this.canConnectTo(neighborState));
    }

    @Override
    public boolean canConnectTo(BlockState state) {
        return state.is(ModTags.CONDUCTIVE_BLOCKS);
    }

    @Override
    public void updateCosmetics(BlockState state, BlockPos pos, BlockPos neighborPos, Level level, boolean isConnectable) {
        int dx = neighborPos.getX()-pos.getX();
        int dy = neighborPos.getY()-pos.getY();
        int dz = neighborPos.getZ()-pos.getZ();

        state.setValue(getPropForDir(Direction.fromDelta(dx, dy, dz)), isConnectable);
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
