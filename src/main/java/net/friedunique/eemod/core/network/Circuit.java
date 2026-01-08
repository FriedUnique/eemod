package net.friedunique.eemod.core.network;

import net.friedunique.eemod.core.Components;
import net.minecraft.core.BlockPos;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class Circuit {
    // A unique ID for saving/debugging
    public final UUID network_id = UUID.randomUUID();
    // The Matrix/Solver state for this grid
    public double totalVoltage = 0;
    public boolean needsUpdate = false;

    private final Set<Node> nodeEntities = new HashSet<>();
    private final Set<Edge> edgeEntities = new HashSet<>();

    public void addNode(Node node) {
        nodeEntities.add(node);
    }
    public void addEdge(Edge edge) {
        edgeEntities.add(edge);
    }
    public void removeNode(BlockPos pos){
        nodeEntities.removeIf(node -> pos.equals(node.position));
        edgeEntities.removeIf(edge -> edge.nodeOrigin.position == pos);
    }

    public void addAllNodes(Set<Node> nodes) {
        nodeEntities.addAll(nodes);
    }
    public Set<Node> getNodes(){return nodeEntities;}
    public Set<Edge> getEdges(){return edgeEntities;}



    public void refresh(){
        List<Node> nodeList = new ArrayList<>(nodeEntities);
        for(int i = 0; i<nodeEntities.size(); i++){
            nodeList.get(i).resetSolverData();
        }
    }

    // This is where your Solver will live
    public void Knotenpotentialverfahren() {
        int n = nodeEntities.size();
        if (n == 0) return;
        System.out.println("\n\n---Solving Circuit with " + n + " nodes.---");
        List<Node> nodeList = new ArrayList<>(nodeEntities);

        // Reset solver data
        for(Node node : nodeList) node.resetSolverData();

        SimpleMatrix G = new SimpleMatrix(n,n);
        SimpleMatrix i = new SimpleMatrix(n,1);

        // ==========================================================
        //  Voltage / Currant Sources
        // ==========================================================
        for (Node sourceNode : nodeList) {

            if (sourceNode.type == Components.ComponentType.SOURCE) {
                System.out.println("DEBUG: Found a Source Node at " + sourceNode.position);

                Node posNode = sourceNode.positiveNode;
                Node negNode = sourceNode.negativeNode;
                // make sure that they are set to true;



                if (posNode == null) System.out.println("  -> Positive Neighbor is NULL! (Front connection missing)");
                else System.out.println("  -> Positive Neighbor found: " + posNode.position);

                if (negNode == null) System.out.println("  -> Negative Neighbor is NULL! (Back connection missing)");
                else System.out.println("  -> Negative Neighbor found: " + negNode.position);

                if (posNode == null || negNode == null) {
                    System.out.println("  -> SKIPPING Injection because circuit is open.");
                    continue;
                }

                posNode.name = "Positive Node";
                negNode.name = "Negative Node";

                int u = nodeList.indexOf(posNode);
                int v = nodeList.indexOf(negNode);

                // If the neighbors aren't part of this circuit (error state), skip
                // when breaking and replacing the wire this error will appear
                if (u == -1) System.out.println("  CRITICAL ERROR: Positive neighbor is not in this circuit!");
                if (v == -1) System.out.println("  CRITICAL ERROR: Negative neighbor is not in this circuit!");
                if (u == -1 || v == -1) continue;

                // 3. Spannungsquelle <-> Stromquelle
                // need to handle stromquelle as well
                double conductance = 1.0 / sourceNode.internalRestistance;
                double current = sourceNode.sourceVoltage * conductance;

                // 4. Inject Current (Vector i)
                // Push current INTO positive, Pull current FROM negative
                i.set(u, 0, i.get(u, 0) + current);
                i.set(v, 0, i.get(v, 0) - current); // this will be zero anyway..., check maybe?

                // 5. Inject Internal Resistance (Matrix G)
                // The battery acts as a resistor between u and v
                G.set(u, u, G.get(u, u) + conductance);
                G.set(v, v, G.get(v, v) + conductance);

                G.set(u, v, G.get(u, v) - conductance);
                G.set(v, u, G.get(v, u) - conductance);

                negNode.isTouchingNegativeTerminal = true;
                posNode.isTouchingPositiveTerminal = true;
            }
        }

        // ==========================================================
        //  Normal Wire Edges
        // ==========================================================
        for (Edge edge : edgeEntities) {
            int u = nodeList.indexOf(edge.nodeOrigin);
            int v = nodeList.indexOf(edge.nodeEnd);

            double leitwert = 1.0 / edge.resistance;
            if (u == -1) System.out.println("  CRITICAL ERROR: 1");
            if (v == -1) System.out.println("  CRITICAL ERROR: 2");
            if (u == -1 || v == -1) continue;

            System.out.println(""+u+" "+v+" "+" "+leitwert);

            G.set(u, u, G.get(u, u) + leitwert);
            G.set(v, v, G.get(v, v) + leitwert);
            G.set(u, v, G.get(u, v) - leitwert);
            G.set(v, u, G.get(v, u) - leitwert);
        }

        for (int idx = 0; idx < n; idx++) {
            // check if the diagonal is 0 -> edge missing
            // edge is missing for sources -> if there were an edge there would be a short circuit -> 0V
            if (G.get(idx, idx) == 0.0) {
                G.set(idx, idx, 1.0);

                System.out.println("Warning: Node ("+idx+") " + nodeList.get(idx).name + " is floating! Stabilizing...");
            }
        }

        // 4. Apply Ground (Boundary Condition)
        // Better Ground Selection
        int groundNodeIndex = -1;

        for(int idx=0; idx<n; idx++) {
            if (nodeList.get(idx).isTouchingNegativeTerminal) {
                groundNodeIndex = idx;
                break;
            }
        }

        if (groundNodeIndex == -1) groundNodeIndex = n - 1;

        // ground node is zero
        for (int idx = 0; idx < n; idx++) {
            G.set(groundNodeIndex, idx, 0);
            G.set(idx, groundNodeIndex, 0);
        }
        // Set diagonal to 1 and target to 0 (Equation: 1 * V_ground = 0)
        G.set(groundNodeIndex, groundNodeIndex, 1.0);
        i.set(groundNodeIndex, 0, 0);

        try {
            // Gradinaru
            SimpleMatrix x = G.solve(i);

            // Retrieve voltages
            for(int idx = 0; idx < n; idx++) {
                nodeList.get(idx).simulatedVoltage = x.get(idx);
            }
        } catch (Exception e) {
            System.err.println("Circuit solver failed: " + e.getMessage());
        }
        printDebug();

    }

    public void printDebug() {
        System.out.println("--- Circuit Simulation Results ---\n");

        List<Node> nodeList = new ArrayList<>(nodeEntities);

        for (int i = 0; i < nodeEntities.size(); i++) {
            Node node = nodeList.get(i);
            System.out.println("Node ("+i+") " + nodeList.get(i).name + " Voltage: " + node.simulatedVoltage);
        }
    }

}