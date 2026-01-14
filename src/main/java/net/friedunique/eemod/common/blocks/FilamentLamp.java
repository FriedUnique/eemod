package net.friedunique.eemod.common.blocks;

import net.friedunique.eemod.common.ModTags;
import net.friedunique.eemod.common.entities.FilamentBlockEntity;
import net.friedunique.eemod.common.entities.ModBlockEntities;
import net.friedunique.eemod.core.Components.*;
import net.friedunique.eemod.core.ElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nullable;

public class FilamentLamp extends ElectricalBlock implements EntityBlock {

    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    public FilamentLamp(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0));
    }


    @Override
    public boolean canConnectTo(BlockState state) {
        return state.is(ModTags.CONDUCTIVE_BLOCKS);
    }

    @Override
    public void updateCosmetics(BlockState state, BlockPos pos, BlockPos neighborPos, Level level, boolean isConnectable) {

    }

    @Override
    public NodeDefinition getNodeDefinition(Level level, BlockPos pos) {
        boolean[] isTouching = checkNeighborTerminals(level, pos);
        return new NodeDefinition(ComponentType.LOAD, "Lamp", 9.5, 0.00045, 60, isTouching[0], isTouching[1]);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL);
    }



    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // This creates the "brain" for the block when it's placed
        return new FilamentBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return type == ModBlockEntities.LAMP_BE.get() ? (lvl, pos, st, be) -> {
            if (be instanceof FilamentBlockEntity lamp) {
                FilamentBlockEntity.serverTick(lvl, pos, st, lamp);
            }
        } : null;
    }
}
