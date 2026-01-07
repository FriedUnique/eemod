package net.friedunique.eemod.core.network;

import net.friedunique.eemod.common.blocks.DebugVoltageSource;
import net.friedunique.eemod.core.Components;
import net.friedunique.eemod.core.ElectricalBlock;
import net.friedunique.eemod.core.ElectricalBlock.NodeDefinition;
import net.friedunique.eemod.core.IElectricalConductor;
import net.friedunique.eemod.core.IVoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.Collectors;


public class NetworkManager {
    // on placement of a wire or a network block discover other blocks which are network blocks
    // if found add that block to that network, if not found then make a new network
    // the circuit class creates the kirchhoffsche regeln

    private static final Map<ResourceKey<Level>, NetworkManager> INSTANCES = new HashMap<>();

    public static NetworkManager get(Level level) {
        return INSTANCES.computeIfAbsent(level.dimension(), k -> new NetworkManager());
    }

    // Lookup table: Given a BlockPos, which Node object is it?
    private final Map<BlockPos, Node> posToNode = new HashMap<>();

    // Lookup table: Given a BlockPos, which Circuit ID does it belong to?
    private final Map<BlockPos, Circuit> posToCircuit = new HashMap<>();

    // All active circuits
    private final Set<Circuit> circuits = new HashSet<>();

    public final List<Circuit> getCircuits(){
        return new ArrayList<>(circuits);
    }

    public final List<Node> getNodes(){
        return new ArrayList<>(posToNode.values());
    }

    private boolean updateAll = false;

    public void run(){
        for (int i = 0; i < circuits.size(); i++){
            try {
                circuits.iterator().next().Knotenpotentialverfahren();
            }catch (Exception e){
                System.err.println("--------\n" + e);
            }
        }
    }

    public void refreshAll(Level level){

        updateSourceConnections(level);

//        for (int i = 0; i < circuits.size(); i++){
//            circuits.iterator().next().refresh();
//        }

    }


    private void updateSourceConnections(Level level){
        List<Node> sourceNodes = posToNode.values().stream().filter(node -> node.type.equals(Components.ComponentType.SOURCE)).collect(Collectors.toList());
        for(Node sourceNode : sourceNodes){
            BlockState state = level.getBlockState(sourceNode.position);

            // Safety Check: Is this actually our source block?
            if (!(state.getBlock() instanceof DebugVoltageSource)) return;

            // 2. Get the Facing Direction
            Direction facing = state.getValue(DebugVoltageSource.FACING);

            // 3. Calculate Neighbor Positions
            // FRONT = Positive Terminal
            BlockPos posFront = sourceNode.position.relative(facing);
            // BACK = Negative Terminal
            BlockPos posBack = sourceNode.position.relative(facing.getOpposite());

            // 4. Lookup the Nodes in your global map
            // (Assuming you have a Map<BlockPos, Node> called 'nodeMap')
            Node positiveNode = posToNode.get(posFront);
            Node negativeNode = posToNode.get(posBack);

            // 5. Assign them to the Source Node
            // (Only if they exist - i.e., there is a wire connected there)
            sourceNode.positiveNode = positiveNode;
            sourceNode.negativeNode = negativeNode;
        }
    }


    public void addNode(Level level, BlockPos pos, ElectricalBlock block){
        if (posToNode.containsKey(pos)) return;

        NodeDefinition nodeDefinition = block.getNodeDefinition(level, pos);
        Node newNode = new Node(pos);
        newNode.type = nodeDefinition.type();
        newNode.internalRestistance = nodeDefinition.resistance();
        newNode.sourceVoltage = nodeDefinition.sourceVoltage();

        posToNode.put(pos, newNode);


        // 2. Scan for neighbors
        Set<Circuit> neighborCircuits = new HashSet<>();
        List<BlockPos> neighborNodes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            // Check if we have a registered node there
            if (posToNode.containsKey(neighborPos)) {
                Circuit neighborCircuit = posToCircuit.get(neighborPos);
                if (neighborCircuit != null) {
                    neighborNodes.add(neighborPos);
                    neighborCircuits.add(neighborCircuit);
                }
            }
        }

        // 3. Handle the Grid Logic
        if (neighborCircuits.isEmpty()) {
            // SCENARIO A: Brand new isolated wire
            createNewCircuit(newNode);
        } else {
            // SCENARIO C: Bridging two or more separate networks (MERGE)
            List<Node> validNeighborNodes = new ArrayList<>();
            for (BlockPos p : neighborNodes) {
                if (posToNode.containsKey(p)) {
                    validNeighborNodes.add(posToNode.get(p));
                }
            }

            mergeCircuits(neighborCircuits, newNode, validNeighborNodes);
        }
    }



    public void removeNode(BlockPos pos){
        System.out.println("REMOVE BLOCK CALLED");
        if(!posToCircuit.containsKey(pos)){
            return;
        }

        Circuit thisCircuit = posToCircuit.get(pos);

        // Scan for neighbors
        Set<Circuit> neighborCircuits = new HashSet<>();
        List<BlockPos> connectedNeighbors = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            // Check if we have a registered node there
            if (posToNode.containsKey(neighborPos)) {
                Circuit neighborCircuit = posToCircuit.get(neighborPos);
                if (neighborCircuit == thisCircuit) {
                    connectedNeighbors.add(neighborPos);
                    neighborCircuits.add(neighborCircuit);
                }
            }
        }

        // Handle the Grid Logic
        if (neighborCircuits.isEmpty()) {
            // SCENARIO A: Last node in a wire
            circuits.remove(thisCircuit);
            posToNode.remove(pos);
        } else if (neighborCircuits.size() == 1) {
            // SCENARIO B: Removing a node from an exsisting wire
            thisCircuit.removeNode(pos);
        } else {
            // SCENARIO C: Bridging two or more separate networks (MERGE)
            unmergeCircuits(thisCircuit, pos, connectedNeighbors);
        }
        updateAll = true;

    }


    // --- Helper Methods ---

    public void tick() {
        if(updateAll){
            updateAll = false;

        }
    }

    public double getVoltageAt(BlockPos position){
        return 0;
    }

    public double getCurrentAt(BlockPos position){
        return 0;
    }

    private void createEdges(Circuit circuit, Node centerNode, List<Node> neighbors) {
        // do not connect source to itself ...
        if (centerNode.type == Components.ComponentType.SOURCE) {
            return;
        }

        for (Node neighbor : neighbors) {
            // 2. If the NEIGHBOR is a source, do not create an edge to it!
            if (neighbor.type == Components.ComponentType.SOURCE) {
                continue;
            }

            // 3. Normal Connection (Wire-to-Wire or Wire-to-Machine)
            // This is safe to add.
            double edgeResistance = centerNode.internalRestistance + neighbor.internalRestistance;

            Edge e = new Edge(centerNode, neighbor, edgeResistance);
            circuit.addEdge(e);

            // Register connections for traversal
//            centerNode.addConnection(e);
//            neighbor.addConnection(e);
        }
    }

    private void createNewCircuit(Node node) {
        Circuit c = new Circuit();
        c.addNode(node);
        circuits.add(c);
        posToCircuit.put(node.position, c);
    }

    private void addToCircuit(Circuit c, Node node, Edge edge) {
        c.addNode(node);
        c.addEdge(edge);
        posToCircuit.put(node.position, c);
    }

    // The complex part: Combine multiple sets into one
    private void mergeCircuits(Set<Circuit> circuitsToMerge, Node bridgeNode, List<Node> neighborNodes) {
        // 1. Pick the "Master" Circuit (Optimization: keep the largest one)
        Circuit master = circuitsToMerge.stream()
                .max(Comparator.comparingInt(c -> c.getNodes().size()))
                .orElseThrow(() -> new IllegalStateException("Attempting to merge empty set of circuits"));

        // 2. Absorb all smaller circuits into the Master
        for (Circuit other : circuitsToMerge) {
            if (other == master) continue;

            // Move all nodes and EDGES from 'other' to 'master'
            master.getNodes().addAll(other.getNodes());
            master.getEdges().addAll(other.getEdges());

            // Update the lookup map so these nodes point to the new master circuit
            for (Node node : other.getNodes()) {
                // Assuming your Node object has a reference to its circuit:
                node.parentCircuit = master;
                // Or if you use a map: posToCircuit.put(node.getPos(), master);
            }

            // Delete the old circuit object
            this.circuits.remove(other);
        }

        // 3. Add the new Bridge Node to the Master
        master.addNode(bridgeNode);
        bridgeNode.parentCircuit = master; // Update reference

        // 4. Create Edges for ALL connections (The part you were missing)
        createEdges(master, bridgeNode, neighborNodes);
//        for (Node neighbor : neighborNodes) {
//
//            Edge edgeToBridge = new Edge(neighbor, bridgeNode, bridgeNode.internalRestistance);
//            master.addEdge(edgeToBridge);
//
//            // Example: Adding edge from BridgeNode -> Neighbor
//            Edge edgeToNeighbor = new Edge(bridgeNode, neighbor, neighbor.internalRestistance);
//            master.addEdge(edgeToNeighbor);
//        }

        // 5. Trigger update
        master.needsUpdate = true;
    }

    public void unmergeCircuits(Circuit originalCircuit, BlockPos brokenBlockPos, List<BlockPos> connectedNeighbors) {
        // 1. Remove the broken node from the original circuit data
        originalCircuit.removeNode(brokenBlockPos);

        // Set of positions we have already re-assigned to a new circuit
        Set<BlockPos> processedPositions = new HashSet<>();

        // Get the set of all valid positions remaining in the old circuit
        // (We need this to ensure we don't accidentally grab wires from a DIFFERENT circuit nearby)
        Set<BlockPos> validOldPositions = originalCircuit.getNodes().stream().map(node -> node.position).collect(Collectors.toSet());

        for (BlockPos neighborPos : connectedNeighbors) {

            // If we already processed this neighbor (because it was part of a loop we just found), skip it
            if (processedPositions.contains(neighborPos)) {
                continue;
            }

            // 2. Run Flood Fill to get the SET OF NODES for this new segment
            Set<Node> newNetworkNodes = performFloodFill(neighborPos, validOldPositions, this.posToNode);

            if (!newNetworkNodes.isEmpty()) {
                // 3. Create the new circuit object
                Circuit newCircuit = new Circuit();
                newCircuit.addAllNodes(newNetworkNodes); // Assuming you have a setter for the whole set

                // Register it to your main list
                this.circuits.add(newCircuit);

                // 4. Update the global map pointers?
                // If your Node object stores a reference to its parent Circuit, update it now:
                for (Node n : newNetworkNodes) {
                    n.parentCircuit = newCircuit;
                    processedPositions.add(n.position); // Mark as processed
                }
            }
        }

        // 5. Delete the old circuit
        this.circuits.remove(originalCircuit);
    }

    private Set<Node> performFloodFill(BlockPos startPos, Set<BlockPos> allowedPositions, Map<BlockPos, Node> globalNodeMap) {
        Set<Node> foundNodes = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Initialize
        queue.add(startPos);
        visited.add(startPos);

        // Add the starting node immediately
        if (globalNodeMap.containsKey(startPos)) {
            foundNodes.add(globalNodeMap.get(startPos));
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Check neighbors (Standard 6 directions)
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = current.relative(dir);

                // 1. Must be part of the ORIGINAL circuit (allowedPositions)
                // 2. Must not have been visited yet in this specific search
                if (allowedPositions.contains(adjacent) && !visited.contains(adjacent)) {

                    visited.add(adjacent);
                    queue.add(adjacent);

                    // Retrieve the actual Node object and add it to our result
                    Node node = globalNodeMap.get(adjacent);
                    if (node != null) {
                        foundNodes.add(node);
                    }
                }
            }
        }
        return foundNodes;
    }
}


/*
*     public void addSource(BlockPos pos, IVoltageSource v_source){
        if (posToNode.containsKey(pos)) return;

        Node newNode = new Node(pos, "Source");
        newNode.type = Components.ComponentType.SOURCE;
        newNode.knownCurrent = v_source.getShortCircuitCurrent();
        newNode.sourceVoltage = 12;
        newNode.internalRestistance = v_source.getInternalResistance();

        posToNode.put(pos, newNode);

        // 2. Scan for neighbors
        Set<Circuit> neighborCircuits = new HashSet<>();
        List<BlockPos> neighborNodes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            // Check if we have a registered node there
            if (posToNode.containsKey(neighborPos)) {
                Circuit neighborCircuit = posToCircuit.get(neighborPos);
                if (neighborCircuit != null) {
                    neighborNodes.add(neighborPos);
                    neighborCircuits.add(neighborCircuit);
                }
            }
        }

        // 3. Handle the Grid Logic
        if (neighborCircuits.isEmpty()) {
            // SCENARIO A: Brand new isolated wire
            createNewCircuit(newNode);
        } else if (neighborCircuits.size() == 1) {
            // SCENARIO B: Extending an existing wire
            Circuit existing = neighborCircuits.iterator().next();
            Edge edgeAB = new Edge(posToNode.get(neighborNodes.getFirst()), newNode, newNode.internalRestistance);
            addToCircuit(existing, newNode, edgeAB);
            System.out.print("Node added to a circuit!");
        } else {
            // SCENARIO C: Bridging two or more separate networks (MERGE)
            List<Node> validNeighborNodes = new ArrayList<>();
            for (BlockPos p : neighborNodes) {
                if (posToNode.containsKey(p)) {
                    validNeighborNodes.add(posToNode.get(p));
                }
            }

            mergeCircuits(neighborCircuits, newNode, validNeighborNodes);
            System.out.print("Node bridging between two or more networks!");
        }
    }

    public void addConductor(BlockPos pos, IElectricalConductor conductor, boolean isTouchingFrontSide, boolean isTouchingBackside) {
        if (posToNode.containsKey(pos)) return; // Already exists

        // 1. Create the Node wrapper
        Node newNode = new Node(pos);
        newNode.type = Components.ComponentType.CONDUCTOR;
        newNode.internalRestistance = conductor.getBaseResistance();
        newNode.isTouchingPositiveTerminal = isTouchingFrontSide;
        newNode.isTouchingNegativeTerminal = isTouchingBackside;
        posToNode.put(pos, newNode);

        // 2. Scan for neighbors
        Set<Circuit> neighborCircuits = new HashSet<>();
        List<BlockPos> neighborNodes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            // Check if we have a registered node there
            if (posToNode.containsKey(neighborPos)) {
                Circuit neighborCircuit = posToCircuit.get(neighborPos);
                if (neighborCircuit != null) {
                    neighborNodes.add(neighborPos);
                    neighborCircuits.add(neighborCircuit);
                }
            }
        }



        // 3. Handle the Grid Logic
        if (neighborCircuits.isEmpty()) {
            // SCENARIO A: Brand new isolated wire
            createNewCircuit(newNode);
        } else if (neighborCircuits.size() == 1) {
            // SCENARIO B: Extending an existing wire
            Circuit existing = neighborCircuits.iterator().next();
            Edge edgeAB = new Edge(newNode, posToNode.get(neighborNodes.getFirst()), newNode.internalRestistance);
            addToCircuit(existing, newNode, edgeAB);
        } else {
            // SCENARIO C: Bridging two or more separate networks (MERGE)

            List<Node> validNeighborNodes = new ArrayList<>();
            for (BlockPos p : neighborNodes) {
                if (posToNode.containsKey(p)) {
                    validNeighborNodes.add(posToNode.get(p));
                }
            }

            mergeCircuits(neighborCircuits, newNode, validNeighborNodes);

        }
    }

*
*
* */