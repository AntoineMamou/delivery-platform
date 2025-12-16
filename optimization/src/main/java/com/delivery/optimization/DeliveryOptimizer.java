package com.delivery.optimization;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.delivery.core.model.Delivery;
import com.delivery.core.model.Edge;
import com.delivery.core.model.Graph;
import com.delivery.core.model.Node;
import com.delivery.core.model.Route;
import com.delivery.core.model.Truck;

public class DeliveryOptimizer {

    private static final double AVERAGE_SPEED_KMH = 50.0;

    // Structure pour retourner Distances + Chemins
    public static class TopologyAnalysis {
        public Map<Integer, Map<Integer, Double>> distances;
        public Integer[][] nextNodeMatrix;

        public TopologyAnalysis(Map<Integer, Map<Integer, Double>> dist, Integer[][] next) {
            this.distances = dist;
            this.nextNodeMatrix = next;
        }
    }

    private static class TruckState {
        Truck truck;
        int currentLocationId;
        LocalTime currentTime;
        int currentLoad;
        double currentDistance;
        List<Delivery> assignedDeliveries;
        List<Integer> fullPathNodeIds; // <--- NOUVEAU : L'itinéraire complet

        public TruckState(Truck truck, int warehouseId) {
            this.truck = truck;
            this.currentLocationId = warehouseId;
            this.currentTime = LocalTime.of(8, 0); 
            this.currentLoad = 0;
            this.currentDistance = 0.0;
            this.assignedDeliveries = new ArrayList<>();
            this.fullPathNodeIds = new ArrayList<>();
            this.fullPathNodeIds.add(warehouseId); // Point de départ
        }
    }

    public static List<Route> optimize(List<Delivery> deliveries, List<Truck> trucks, int warehouse, Graph graph) {
        System.out.println(">>> [OPTIMIZER] Calcul avec itinéraires complets...");

        // 1. Analyse Topologique (Distance + Chemins)
        TopologyAnalysis topology = analyzeGraph(graph);
        Map<Integer, Map<Integer, Double>> distanceMatrix = topology.distances;
        Integer[][] nextMatrix = topology.nextNodeMatrix;

        List<TruckState> fleetState = new ArrayList<>();
        for (Truck t : trucks) fleetState.add(new TruckState(t, warehouse));

        List<Delivery> pendingDeliveries = new ArrayList<>(deliveries);
        pendingDeliveries.sort(Comparator.comparing(Delivery::getEarliestDeliveryTime));

        boolean deliveryAssigned = true;

        while (!pendingDeliveries.isEmpty() && deliveryAssigned) {
            deliveryAssigned = false;
            TruckState bestTruckState = null;
            Delivery bestDelivery = null;
            double bestScore = Double.MAX_VALUE;

            for (Delivery delivery : pendingDeliveries) {
                for (TruckState state : fleetState) {
                    double distTo = getDistance(distanceMatrix, state.currentLocationId, delivery.getAddressNodeId());
                    double distHome = getDistance(distanceMatrix, delivery.getAddressNodeId(), warehouse);

                    if (distTo >= Double.MAX_VALUE / 2) continue;
                    if (state.currentLoad + 1 > state.truck.getDeliveryCapacity()) continue;
                    if (state.currentDistance + distTo + distHome > state.truck.getMaxDistance()) continue;

                    long travelMinutes = (long) ((distTo / AVERAGE_SPEED_KMH) * 60.0);
                    LocalTime arrival = state.currentTime.plusMinutes(travelMinutes);
                    
                    long waitMinutes = 0;
                    if (arrival.isBefore(delivery.getEarliestDeliveryTime())) {
                        waitMinutes = ChronoUnit.MINUTES.between(arrival, delivery.getEarliestDeliveryTime());
                        arrival = delivery.getEarliestDeliveryTime();
                    }

                    if (arrival.isAfter(delivery.getlatestDeliveryTime())) continue;

                    long timeToDeadline = ChronoUnit.MINUTES.between(arrival, delivery.getlatestDeliveryTime());
                    double score = distTo + (waitMinutes * 0.5) - (timeToDeadline * 0.1); 

                    if (score < bestScore) {
                        bestScore = score;
                        bestTruckState = state;
                        bestDelivery = delivery;
                    }
                }
            }

            if (bestTruckState != null && bestDelivery != null) {
                // On passe la matrice 'next' pour reconstruire le chemin
                addDeliveryToTruck(bestTruckState, bestDelivery, distanceMatrix, nextMatrix, warehouse);
                pendingDeliveries.remove(bestDelivery);
                deliveryAssigned = true;
            }
        }

        // Création des Routes finales
        List<Route> routes = new ArrayList<>();
        for (TruckState state : fleetState) {
            if (!state.assignedDeliveries.isEmpty()) {
                // ICI : Vous devrez probablement modifier votre classe Route pour accepter 'fullPathNodeIds'
                // Pour l'instant, je l'imprime juste pour preuve
                System.out.println("Camion " + state.truck.getId() + " itinéraire : " + state.fullPathNodeIds);
                
                routes.add(new Route(state.truck, warehouse, state.assignedDeliveries,state.fullPathNodeIds));
            }
        }
        return routes;
    }

    // --- Ajout de la livraison et calcul du chemin détaillé ---
    private static void addDeliveryToTruck(TruckState state, Delivery delivery, 
                                           Map<Integer, Map<Integer, Double>> distMatrix, 
                                           Integer[][] nextMatrix, int warehouse) {
        
        // 1. Récupérer la distance
        double dist = getDistance(distMatrix, state.currentLocationId, delivery.getAddressNodeId());
        
        // 2. Reconstruire le chemin détaillé (Nœuds intermédiaires)
        List<Integer> path = reconstructPath(state.currentLocationId, delivery.getAddressNodeId(), nextMatrix);
        
        // On ajoute le chemin à l'historique du camion (sans répéter le nœud de départ actuel)
        if (!path.isEmpty()) {
            state.fullPathNodeIds.addAll(path.subList(1, path.size()));
        }

        // Mise à jour habituelle
        long travelMinutes = (long) ((dist / AVERAGE_SPEED_KMH) * 60.0);
        state.currentDistance += dist;
        state.currentLoad++;
        state.currentLocationId = delivery.getAddressNodeId();
        
        LocalTime arrival = state.currentTime.plusMinutes(travelMinutes);
        if (arrival.isBefore(delivery.getEarliestDeliveryTime())) {
            arrival = delivery.getEarliestDeliveryTime();
        }
        delivery.setEstimatedArrivalTime(arrival);
        state.currentTime = arrival.plusMinutes(10);
        state.assignedDeliveries.add(delivery);
    }

    // --- Algorithme pour reconstruire le chemin à partir de la matrice 'Next' ---
    private static List<Integer> reconstructPath(int start, int end, Integer[][] next) {
        List<Integer> path = new ArrayList<>();
        if (next[start][end] == null) return path; // Pas de chemin

        path.add(start);
        int current = start;
        while (current != end) {
            current = next[current][end];
            path.add(current);
        }
        return path;
    }

    // --- Helpers ---

    private static double getDistance(Map<Integer, Map<Integer, Double>> matrix, int fromId, int toId) {
        if (fromId == toId) return 0.0;
        return matrix.getOrDefault(fromId, Collections.emptyMap()).getOrDefault(toId, Double.MAX_VALUE);
    }

    // Remplaçant de buildDistanceMatrix
    private static TopologyAnalysis analyzeGraph(Graph graph) {
        List<Integer> nodeIds = graph.getNodes().stream().map(Node::getId).collect(Collectors.toList());
        int maxId = nodeIds.stream().max(Integer::compareTo).orElse(0);
        
        double[][] dist = new double[maxId + 1][maxId + 1];
        Integer[][] next = new Integer[maxId + 1][maxId + 1]; // Matrice pour le chemin
        double INF = Double.MAX_VALUE / 2;

        // Initialisation
        for (int i = 0; i <= maxId; i++) {
            Arrays.fill(dist[i], INF);
            Arrays.fill(next[i], null);
            dist[i][i] = 0.0;
        }

        // Remplissage avec les arêtes directes
        for (Edge e : graph.getEdges()) {
            if (e.getSourceNodeId() > maxId || e.getTargetNodeId() > maxId) continue;
            
            double len = e.getLength();
            int u = e.getSourceNodeId();
            int v = e.getTargetNodeId();
            
            // Sens U -> V
            if (len < dist[u][v]) {
                dist[u][v] = len;
                next[u][v] = v; // Si je suis à U et veux aller à V, je vais directement à V
            }
            // Sens V -> U
            if (len < dist[v][u]) {
                dist[v][u] = len;
                next[v][u] = u;
            }
        }

        // Floyd-Warshall avec reconstruction de chemin
        for (Integer k : nodeIds) {
            for (Integer i : nodeIds) {
                for (Integer j : nodeIds) {
                    if (dist[i][k] != INF && dist[k][j] != INF && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k]; // Pour aller de I à J, je passe par là où je passais pour aller de I à K
                    }
                }
            }
        }

        // Conversion Map pour compatibilité existante
        Map<Integer, Map<Integer, Double>> resultMatrix = new HashMap<>();
        for (Integer i : nodeIds) {
            resultMatrix.put(i, new HashMap<>());
            for (Integer j : nodeIds) if (dist[i][j] != INF) resultMatrix.get(i).put(j, dist[i][j]);
        }
        
        return new TopologyAnalysis(resultMatrix, next);
    }
}