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
        System.out.println("Solving Circuit with " + n + " nodes.");
        List<Node> nodeList = new ArrayList<>(nodeEntities);

        // Reset solver data
        for(Node node : nodeList) node.resetSolverData();

        SimpleMatrix G = new SimpleMatrix(n,n);
        SimpleMatrix i = new SimpleMatrix(n,1);

        // ==========================================================
        // STEP 1: Process Voltage Sources (The new part)
        // ==========================================================
        for (Node sourceNode : nodeList) {

            if (sourceNode.type == Components.ComponentType.SOURCE) {
                System.out.println("DEBUG: Found a Source Node at " + sourceNode.position);
                // 1. Get the connected wire nodes
                Node posNode = sourceNode.positiveNode;
                Node negNode = sourceNode.negativeNode;

                if (posNode == null) System.out.println("  -> Positive Neighbor is NULL! (Front connection missing)");
                else System.out.println("  -> Positive Neighbor found: " + posNode.position);

                if (negNode == null) System.out.println("  -> Negative Neighbor is NULL! (Back connection missing)");
                else System.out.println("  -> Negative Neighbor found: " + negNode.position);

                if (posNode == null || negNode == null) {
                    System.out.println("  -> SKIPPING Injection because circuit is open.");
                    continue;
                }

                // Safety check: Is the battery actually connected?
                if (posNode == null || negNode == null) continue;

                // 2. Find their Matrix Indices
                int u = nodeList.indexOf(posNode);
                int v = nodeList.indexOf(negNode);

                // If the neighbors aren't part of this circuit (error state), skip
                if (u == -1) System.out.println("  CRITICAL ERROR: Positive neighbor is not in this circuit!");
                if (v == -1) System.out.println("  CRITICAL ERROR: Negative neighbor is not in this circuit!");
                if (u == -1 || v == -1) continue;

                // 3. Calculate Norton Equivalent
                // Current Source (I) = V / R_internal
                // Conductance (G)    = 1 / R_internal
                double conductance = 1.0 / sourceNode.internalRestistance;
                double current = sourceNode.sourceVoltage * conductance;

                // 4. Inject Current (Vector i)
                // Push current INTO positive, Pull current FROM negative
                i.set(u, 0, i.get(u, 0) + current);
                i.set(v, 0, i.get(v, 0) - current);

                // 5. Inject Internal Resistance (Matrix G)
                // The battery acts as a resistor between u and v
                G.set(u, u, G.get(u, u) + conductance);
                G.set(v, v, G.get(v, v) + conductance);

                G.set(u, v, G.get(u, v) - conductance);
                G.set(v, u, G.get(v, u) - conductance);

                // 6. Mark the Negative Terminal for Grounding
                // This helps our Ground selection logic later
                negNode.isTouchingNegativeTerminal = true;
            }
        }

        // ==========================================================
        // STEP 2: Process Normal Wire Edges
        // ==========================================================
        for (Edge edge : edgeEntities) {
            int u = nodeList.indexOf(edge.nodeOrigin);
            int v = nodeList.indexOf(edge.nodeEnd);

            double leitwert = 1.0 / edge.resistance;
            if (u == -1) System.out.println("  CRITICAL ERROR:cadfasdsdsa");
            if (v == -1) System.out.println("  CRITICAL ERROR: cccccc");
            if (u == -1 || v == -1) continue;

            System.out.println(""+u+" "+v+" "+" "+leitwert);

            G.set(u, u, G.get(u, u) + leitwert);
            G.set(v, v, G.get(v, v) + leitwert);
            G.set(u, v, G.get(u, v) - leitwert);
            G.set(v, u, G.get(v, u) - leitwert);
        }

        for (int idx = 0; idx < n; idx++) {
            // Check if the diagonal element is 0 (meaning no edges connected to this node)
            if (G.get(idx, idx) == 0.0) {
                // Fix: Set it to 1.0 (Dummy value) so the matrix is not singular
                // This effectively treats the floating node as a separate 0V point.
                G.set(idx, idx, 1.0);

                // Optional: Print warning
                System.out.println("Warning: Node " + nodeList.get(idx).name + " is floating! Stabilizing...");
            }
        }

        // 4. Apply Ground (Boundary Condition)
        // Better Ground Selection
        int groundNodeIndex = -1;

        // 1. Try to find the negative terminal of a battery
                for(int idx=0; idx<n; idx++) {
                    if (nodeList.get(idx).isTouchingNegativeTerminal) { // You need to implement this flag
                        groundNodeIndex = idx;
                        System.out.println("negative node found");
                        break;
                    }
                }

        // 2. Fallback: If no battery, pick the last node
        if (groundNodeIndex == -1) groundNodeIndex = n - 1;

        // Zero out the Ground Row and Column in A
        for (int idx = 0; idx < n; idx++) {
            G.set(groundNodeIndex, idx, 0);
            G.set(idx, groundNodeIndex, 0);
        }
        // Set diagonal to 1 and target to 0 (Equation: 1 * V_ground = 0)
        G.set(groundNodeIndex, groundNodeIndex, 1.0);
        i.set(groundNodeIndex, 0, 0);
        for (int idx = 0; idx < n; idx++) {
            System.out.println(idx + ") Node " + nodeList.get(idx).name + " Current: " + i.get(idx, 0));
        }

        try {
            // This is the magic line that solves the whole circuit
            SimpleMatrix x = G.solve(i);

            // Retrieve voltages
            for(int idx = 0; idx < n; idx++) {
                nodeList.get(idx).sourceVoltage = x.get(idx);
            }
        } catch (Exception e) {
            // Happens if the circuit is disconnected or has no ground
            System.err.println("Circuit solver failed: " + e.getMessage());
        }
        System.out.println(printDebug());

    }

    public String printDebug() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("--- Circuit Simulation Results ---\n");

        List<Node> nodeList = new ArrayList<>(nodeEntities);
        for (int i = 0; i < nodeEntities.size(); i++) {
            Node node = nodeList.get(i);
            stringBuilder.append("Node ").append(node.name).append(" Voltage: ").append(node.sourceVoltage).append("\n");
        }

        return stringBuilder.toString();
    }

}