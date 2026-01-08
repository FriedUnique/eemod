package net.friedunique.eemod.core.network;

import net.friedunique.eemod.core.Components;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public Node(BlockPos pos){
        this(pos, "");
    }

    public Node(BlockPos pos, String name){
        position=pos;
        this.name = name;
        connectedEdges = new ArrayList<>();
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

    public List<Edge> connectedEdges;

    // Helper to see total activity (optional)
    public double getTotalFlow() {
        double sum = 0;
        for(Edge e : connectedEdges) sum += Math.abs(e.simulatedCurrent);
        return sum;
    }


    //only for source
    public double sourceVoltage;
    public Node negativeNode;
    public Node positiveNode;

    public void resetSolverData() {

        this.knownCurrent = 0;
    }
}