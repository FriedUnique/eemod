package net.friedunique.eemod.core.network;

import net.friedunique.eemod.core.Components;

import net.minecraft.core.BlockPos;

public class Node {
    public Node(BlockPos pos){
        this(pos, "");
    }

    public Node(BlockPos pos, String name){
        position=pos;
        this.name = name;
    }

    public BlockPos position;    // Location in Minecraft
    public Circuit parentCircuit;
    public String name;
    public Components.ComponentType type;

    public double simulatedVoltage;
    public double internalRestistance;

    public boolean isTouchingNegativeTerminal = false;
    public boolean isTouchingPositiveTerminal = false;
    public double knownCurrent;


    //only for source
    public double sourceVoltage;
    public Node negativeNode;
    public Node positiveNode;

    public void resetSolverData() {

        this.knownCurrent = 0;
    }
}