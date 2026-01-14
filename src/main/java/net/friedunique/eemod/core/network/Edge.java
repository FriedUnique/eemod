package net.friedunique.eemod.core.network;

public class Edge {
    public Edge(Node nodeOrigin, Node nodeEnd, double resistance){
        this.nodeOrigin = nodeOrigin;
        this.nodeEnd = nodeEnd;
        this.resistance = resistance;
    }

    public Node nodeOrigin;
    public Node nodeEnd;
    public double resistance;    // In Ohms (Ω)
    public double simulatedCurrent;       // Calculated state (Amps)
    public double simulatedEdgeVoltage;

}