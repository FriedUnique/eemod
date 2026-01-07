package net.friedunique.eemod.core.test;

import net.friedunique.eemod.core.Components;
import net.friedunique.eemod.core.network.Circuit;
import net.friedunique.eemod.core.network.Edge;
import net.friedunique.eemod.core.network.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CircuitTest {
    public void Test(Level level){

        Circuit circuit = new Circuit();
        Node n0 = new Node(new BlockPos(0,0,0), " (0) Battery");
        n0.type = Components.ComponentType.SOURCE;
        n0.sourceVoltage = 12;
        n0.internalRestistance = 0.1;

        Node n1 = new Node(new BlockPos(0,0,1), " (1) Positive terminal node");
        n1.internalRestistance = 0.01;
        n1.type = Components.ComponentType.CONDUCTOR;
        n1.isTouchingPositiveTerminal = true;
        n1.isTouchingNegativeTerminal = false;

        Node n2 = new Node(new BlockPos(1,0,1), "2");
        n2.internalRestistance = 0.01;
        n2.type = Components.ComponentType.CONDUCTOR;
        n2.isTouchingPositiveTerminal = false;
        n2.isTouchingNegativeTerminal = false;

        Node n3 = new Node(new BlockPos(1,0,0), "3");
        n3.internalRestistance = 0.01;
        n3.type = Components.ComponentType.CONDUCTOR;
        n3.isTouchingPositiveTerminal = false;
        n3.isTouchingNegativeTerminal = false;

        Node n4 = new Node(new BlockPos(1,0,-1), "4");
        n4.internalRestistance = 0.01;
        n4.type = Components.ComponentType.CONDUCTOR;
        n4.isTouchingPositiveTerminal = false;
        n4.isTouchingNegativeTerminal = false;

        Node n5 = new Node(new BlockPos(0,0,-1), " (5) Negative terminal node gnd");
        n5.internalRestistance = 0.01;
        n5.type = Components.ComponentType.CONDUCTOR;
        n5.isTouchingNegativeTerminal = true;
        n5.isTouchingPositiveTerminal = false;

        n0.positiveNode = n1;
        n0.negativeNode = n5;

        circuit.addNode(n0);
        circuit.addNode(n1);
        circuit.addNode(n2);
        circuit.addNode(n3);
        circuit.addNode(n4);
        circuit.addNode(n5);

//        circuit.addEdge(new Edge(n0, n1, 0.01));
        circuit.addEdge(new Edge(n1, n2, 0.001));
        circuit.addEdge(new Edge(n2, n3, 0.001));
        circuit.addEdge(new Edge(n3, n4, 10));
        circuit.addEdge(new Edge(n4, n5, 0.001));
//        circuit.addEdge(new Edge(n5, n0, 0.01));


        circuit.Knotenpotentialverfahren();
    }



    private void TestOG(){
        // 1. Create the Manager/Circuit
        Circuit circuit = new Circuit();

        // 2. Create Two Nodes
        // Node 0: The "High" side
        // Node 1: The "Low" side (Ground)
        Node n0 = new Node(new BlockPos(0,0,0), "Top wire");
        Node n1 = new Node(new BlockPos(1,0,0), "bottom wire gnd");

        n0.isTouchingPositiveTerminal = true;
        n1.isTouchingNegativeTerminal = true;


        circuit.addNode(n0);
        circuit.addNode(n1);

        // --- STEP 3: Add the Voltage Source (12V) ---
        // We use Norton Equivalent: Current Source + Parallel Resistor
        double sourceVoltage = 12.0;
        double internalRes = 0.1;

        // A. Add the Internal Resistance (Edge between terminals)
        Edge internalEdge = new Edge(n0, n1, internalRes);
        circuit.addEdge(internalEdge);

        // B. Inject Current (I = V / R)
        double current = sourceVoltage / internalRes; // 120 Amps
        n0.knownCurrent += current;  // Push into Top
        n1.knownCurrent -= current;  // Pull from Bottom

        // --- STEP 4: Add the Load (10 Ohm Resistor) ---
        // This is physically connected between the Top and Bottom wires
        Edge loadEdge = new Edge(n0, n1, 10.0);
        circuit.addEdge(loadEdge);

        // --- STEP 5: Solve ---
        circuit.Knotenpotentialverfahren();
//        System.out.println(circuit.printDebug());
    }
}