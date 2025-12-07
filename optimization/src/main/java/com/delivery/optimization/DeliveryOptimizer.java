package com.delivery.optimization;

import com.delivery.core.model.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class DeliveryOptimizer {

    private static final double AVERAGE_SPEED_KMH = 50.0;

    private static class TruckState {
        Truck truck;
        int currentLocationId;
        LocalTime currentTime;
        int currentLoad;
        double currentDistance;
        List<Delivery> assignedDeliveries;

        public TruckState(Truck truck, int warehouseId) {
            this.truck = truck;
            this.currentLocationId = warehouseId;
            this.currentTime = LocalTime.of(8, 0); 
            this.currentLoad = 0;
            this.currentDistance = 0.0;
            this.assignedDeliveries = new ArrayList<>();
        }
    }

    public static List<Route> optimize(List<Delivery> deliveries, List<Truck> trucks, int warehouse, Graph graph) {
       
        // 1. Debug Print : On s'assure que la méthode est bien lancée
    	System.out.println(">>> [OPTIMIZER] DÉMARRAGE DU CALCUL...");
    	System.out.println(">>> [OPTIMIZER] Livraisons reçues : " + deliveries.size());
    	System.out.println(">>> [OPTIMIZER] Camions disponibles : " + trucks.size());
    	
    	// Verification basique
    	if (deliveries.isEmpty() || trucks.isEmpty()) {
    	    System.err.println(">>> [ERREUR] Listes vides ! Annulation.");
    	    return new ArrayList<>();
    	}

        Map<Integer, Map<Integer, Double>> distanceMatrix = buildDistanceMatrix(graph);

        List<TruckState> fleetState = new ArrayList<>();
        for (Truck t : trucks) {
            fleetState.add(new TruckState(t, warehouse));
        }

        List<Delivery> pendingDeliveries = new ArrayList<>(deliveries);
        pendingDeliveries.sort(Comparator.comparing(Delivery::getEarliestDeliveryTime));

        boolean deliveryAssigned = true;
        
        // --- BOUCLE D'OPTIMISATION ---
        while (!pendingDeliveries.isEmpty() && deliveryAssigned) {
            deliveryAssigned = false;
            
            TruckState bestTruckState = null;
            Delivery bestDelivery = null;
            double bestScore = Double.MAX_VALUE;

            for (Delivery delivery : pendingDeliveries) {
                for (TruckState state : fleetState) {
                    
                    double distToCandidate = getDistance(distanceMatrix, state.currentLocationId, delivery.getAddressNodeId());
                    double distHome = getDistance(distanceMatrix, delivery.getAddressNodeId(), warehouse);
                    
                    // Vérification de chemin impossible (Graphe non connecté)
                    if (distToCandidate >= Double.MAX_VALUE / 2 || distHome >= Double.MAX_VALUE / 2) continue;

                    if (state.currentLoad + 1 > state.truck.getDeliveryCapacity()) continue;

                    if (state.currentDistance + distToCandidate + distHome > state.truck.getMaxDistance()) continue;

                    long travelMinutes = (long) ((distToCandidate / AVERAGE_SPEED_KMH) * 60.0);
                    LocalTime arrivalTime = state.currentTime.plusMinutes(travelMinutes);

                    long waitMinutes = 0;
                    if (arrivalTime.isBefore(delivery.getEarliestDeliveryTime())) {
                        waitMinutes = ChronoUnit.MINUTES.between(arrivalTime, delivery.getEarliestDeliveryTime());
                        arrivalTime = delivery.getEarliestDeliveryTime();
                    }

                    if (arrivalTime.isAfter(delivery.getlatestDeliveryTime())) continue;

                    long timeToDeadline = ChronoUnit.MINUTES.between(arrivalTime, delivery.getlatestDeliveryTime());
                    
                    double score = distToCandidate + (waitMinutes * 0.5) - (timeToDeadline * 0.1); 

                    if (score < bestScore) {
                        bestScore = score;
                        bestTruckState = state;
                        bestDelivery = delivery;
                    }
                }
            }

            if (bestTruckState != null && bestDelivery != null) {
                addDeliveryToTruck(bestTruckState, bestDelivery, distanceMatrix, warehouse);
                pendingDeliveries.remove(bestDelivery);
                deliveryAssigned = true;
            }
        }
        
        // --- DIAGNOSTIC DES ÉCHECS (Nouveau bloc) ---
        if (!pendingDeliveries.isEmpty()) {
            System.out.println(">>> [ATTENTION] " + pendingDeliveries.size() + " livraisons IMPOSSIBLES à assigner.");
            runDiagnostics(pendingDeliveries, fleetState, distanceMatrix, warehouse);
        } else {
            System.out.println(">>> [SUCCÈS] Toutes les livraisons sont assignées !");
        }

        List<Route> routes = new ArrayList<>();
        for (TruckState state : fleetState) {
            if (!state.assignedDeliveries.isEmpty()) {
                routes.add(new Route(state.truck, warehouse, state.assignedDeliveries));
            }
        }
        return routes;
    }

    // --- Méthode de Diagnostic ---
    private static void runDiagnostics(List<Delivery> failures, List<TruckState> fleet, Map<Integer, Map<Integer, Double>> matrix, int warehouse) {
        for (Delivery d : failures) {
            System.out.println("--------------------------------------------------");
            System.out.println("Analyse échec pour Livraison ID: " + d.getId() + " (Noeud " + d.getAddressNodeId() + ")");
            System.out.println("Fenêtre horaire: " + d.getEarliestDeliveryTime() + " - " + d.getlatestDeliveryTime());
            
            for (TruckState t : fleet) {
                System.out.println("  > Camion " + t.truck.getId() + " (Charge: " + t.currentLoad + "/" + t.truck.getDeliveryCapacity() + ", Dist: " + String.format("%.2f", t.currentDistance) + ")");
                
                double distTo = getDistance(matrix, t.currentLocationId, d.getAddressNodeId());
                double distHome = getDistance(matrix, d.getAddressNodeId(), warehouse);
                
                if (distTo >= Double.MAX_VALUE / 2) {
                    System.out.println("    [X] Graphe : Pas de chemin (Graphe non connecté ?)");
                    continue;
                }
                
                if (t.currentLoad + 1 > t.truck.getDeliveryCapacity()) {
                    System.out.println("    [X] Capacité : Camion plein.");
                    continue;
                }
                
                double totalDist = t.currentDistance + distTo + distHome;
                if (totalDist > t.truck.getMaxDistance()) {
                    System.out.println("    [X] Distance : Requis " + String.format("%.2f", totalDist) + " > Max " + t.truck.getMaxDistance());
                    continue;
                }
                
                long travelMinutes = (long) ((distTo / AVERAGE_SPEED_KMH) * 60.0);
                LocalTime arrival = t.currentTime.plusMinutes(travelMinutes);
                if (arrival.isBefore(d.getEarliestDeliveryTime())) arrival = d.getEarliestDeliveryTime();
                
                if (arrival.isAfter(d.getlatestDeliveryTime())) {
                     System.out.println("    [X] Temps : Arrivée estimée " + arrival + " est après " + d.getlatestDeliveryTime());
                     continue;
                }
                
                System.out.println("    [?] Inconnu : Ce camion aurait dû pouvoir la prendre...");
            }
        }
    }

    private static void addDeliveryToTruck(TruckState state, Delivery delivery, Map<Integer, Map<Integer, Double>> matrix, int warehouse) {
        double dist = getDistance(matrix, state.currentLocationId, delivery.getAddressNodeId());
        long travelMinutes = (long) ((dist / AVERAGE_SPEED_KMH) * 60.0);
        
        state.currentDistance += dist;
        state.currentLoad++;
        state.currentLocationId = delivery.getAddressNodeId();
        
        LocalTime arrival = state.currentTime.plusMinutes(travelMinutes);
        if (arrival.isBefore(delivery.getEarliestDeliveryTime())) {
            arrival = delivery.getEarliestDeliveryTime();
        }
        
        delivery.setEstimatedArrivalTime(arrival);
        
        state.currentTime = arrival.plusMinutes(10); // Temps de déchargement
        
        state.assignedDeliveries.add(delivery);
    }

    private static double getDistance(Map<Integer, Map<Integer, Double>> matrix, int fromId, int toId) {
        if (fromId == toId) return 0.0;
        return matrix.getOrDefault(fromId, Collections.emptyMap()).getOrDefault(toId, Double.MAX_VALUE);
    }

    private static Map<Integer, Map<Integer, Double>> buildDistanceMatrix(Graph graph) {
        List<Integer> nodeIds = graph.getNodes().stream().map(Node::getId).collect(Collectors.toList());
        
        // Gestion robuste de la taille de la matrice
        int maxId = nodeIds.stream().max(Integer::compareTo).orElse(0);
        double[][] dist = new double[maxId + 1][maxId + 1];
        double INF = Double.MAX_VALUE / 2;

        for (int i = 0; i <= maxId; i++) {
            Arrays.fill(dist[i], INF);
            dist[i][i] = 0;
        }

        for (Edge e : graph.getEdges()) {
            // Sécurité si les IDs des arêtes dépassent les IDs des noeuds connus
            if (e.getSourceNodeId() > maxId || e.getTargetNodeId() > maxId) continue;
            
            int i = e.getSourceNodeId();
            int j = e.getTargetNodeId();
            double length = e.getLength();
            dist[i][j] = Math.min(dist[i][j], length);
            dist[j][i] = Math.min(dist[j][i], length);
        }

        for (Integer k : nodeIds) {
            for (Integer i : nodeIds) {
                for (Integer j : nodeIds) {
                    if (dist[i][k] != INF && dist[k][j] != INF && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        Map<Integer, Map<Integer, Double>> resultMatrix = new HashMap<>();
        for (Integer i : nodeIds) {
            resultMatrix.put(i, new HashMap<>());
            for (Integer j : nodeIds) {
                if (dist[i][j] != INF) {
                    resultMatrix.get(i).put(j, dist[i][j]);
                }
            }
        }
        return resultMatrix;
    }
}