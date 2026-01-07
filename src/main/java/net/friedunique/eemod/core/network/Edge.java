package net.friedunique.eemod.core.network;

public class Edge {
    public Edge(Node nodeOrigin, Node nodeEnd, double resistance){
        this.nodeOrigin = nodeOrigin;
        this.nodeEnd = nodeEnd;
        this.resistance = resistance;
    }

    Node nodeOrigin;
    Node nodeEnd;
    double resistance;    // In Ohms (Ω)
    double current;       // Calculated state (Amps)

    // Ohm's Law helper
    public double getVoltageDrop() {
        return current * resistance;
    }
}