package application;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.managers.DeliveryManager;
import com.delivery.core.managers.TruckManager;
import com.delivery.core.ports.GraphFetcherPort;
import com.delivery.core.ui.DeliveryOptimizerView;
import com.delivery.core_model.Graph;
import com.delivery.core_model.events.OnOptimizationRequest;
import com.delivery.core_model.events.OnOptimizationResult;
import com.delivery.core_model.events.graph.OnFetchGraphRequest;
import com.delivery.core_model.events.graph.OnGraphLoaded;
import com.delivery.datafetcher.GraphFetcherService;
import com.delivery.optimization.OptimizerService;

public class DeliveryOptimizerApp {

    private final EventBus eventBus;
    private final DeliveryOptimizerView view;

    public DeliveryOptimizerApp(EventBus eventBus, DeliveryOptimizerView view) {
        this.eventBus = eventBus;
        this.view = view;
    }

    public void start() {

        DeliveryManager.init(eventBus);
        TruckManager.init(eventBus);

        new OptimizerService(eventBus);
        
        OptimizerService optimizerService = new OptimizerService(eventBus);
        
        eventBus.subscribe(OnOptimizationRequest.class, request -> optimizerService.handleOptimization(request));
        
        GraphFetcherPort graphFetcher = new GraphFetcherService();

        // Subscribe to fetch graph requests
        eventBus.subscribe(OnFetchGraphRequest.class, req -> {
            Graph graph = graphFetcher.fetchGraph("graph.json");
            eventBus.publish(new OnGraphLoaded(graph));
        });

        view.setOptimizeAction(this::onOptimizeRequested);

        subscribeToEvents();
        
        eventBus.publish(new OnFetchGraphRequest());
    }

    private void onOptimizeRequested() {
        eventBus.publish(new OnOptimizationRequest(
            DeliveryManager.getDeliveries(),
            TruckManager.getTrucks(),
            view.getWarehouseNodeId(),
            view.getGraph()
        ));
    }

    private void subscribeToEvents() {
    	eventBus.subscribe(OnOptimizationResult.class,
                result -> view.showOptimizationResult(result)
            );
    }
}