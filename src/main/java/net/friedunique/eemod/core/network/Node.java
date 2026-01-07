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


    public boolean isTouchingNegativeTerminal = false;
    public boolean isTouchingPositiveTerminal = false;
    public Circuit parentCircuit;
    public String name;
    public double sourceVoltage;
    public double knownCurrent;
    public double internalRestistance;
    public Components.ComponentType type;



    //only for source
    public Node negativeNode;
    public Node positiveNode;

    public void resetSolverData() {
        this.knownCurrent = 0;
    }
}