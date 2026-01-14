package net.friedunique.eemod.core.network;

import net.friedunique.eemod.core.Components.SourceType;
import net.friedunique.eemod.core.Components.ComponentType;

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
    public ComponentType componentType;

    // values
    public double simulatedVoltage;
    public double internalRestistance; // temperature may change the internal resistance, resistance at 20°


    public double getResistanceAtTemperature(){
        return internalRestistance*(1+temperatureCoefficient*(temperature-20));
    }



    public double temperatureCoefficient;
    public double temperature = 20; // important for non-ohmic loads such as a lamp
    public double powerUsage;
    public double getTotalFlow() {
        // gemini fix
        // If it's a dead-end (1 connection), return that current.
        if (connectedEdges.size() == 1) {
            return Math.abs(connectedEdges.get(0).simulatedCurrent);
        }

        // If it's a pass-through (2 connections), the sum is double the real flow.
        // We want the average flow through the node.
        double sum = 0;
        for(Edge e : connectedEdges) sum += Math.abs(e.simulatedCurrent);

        // Return the average to represent "Throughput"
        return sum / connectedEdges.size();
    }




    public boolean isTouchingNegativeTerminal = false;
    public boolean isTouchingPositiveTerminal = false;
    public List<Edge> connectedEdges;


    //only for source
    public SourceType sourceType;
    public double sourceVoltage;
    public double sourceCurrent;
    public Node negativeNode;
    public Node positiveNode;

}