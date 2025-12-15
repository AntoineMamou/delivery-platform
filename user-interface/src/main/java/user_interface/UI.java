package user_interface;

import javafx.geometry.Insets;

public final class UI {
	// Prevent instantiation
	private UI() {}
	
	public static final class Sizes {
		public static final double WINDOW_WIDTH = 1500;
		public static final double WINDOW_HEIGHT = 800;
        public static final double FIELD_SMALL = 35;
        public static final double FIELD_LARGE = 60;
    }
	
	public static final class Spacing {
        public static final double SMALL = 5;
        public static final double MEDIUM = 10;
        public static final double LARGE = 20;
    }
	
	public static final class InsetsValues {
        public static final Insets SMALL = new Insets(5);
        public static final Insets MEDIUM = new Insets(10);
        public static final Insets LARGE = new Insets(20);
    }
	
	public static final class SizeRatios{
		public static final double GRAPH_WIDTH_RATIO = 0.7;
		public static final double GRAPH_HEIGHT_RATIO = 0.7;

		public static final double PANEL_WIDTH_RATIO = 0.33;
		public static final double PANEL_HEIGHT_RATIO = 0.33;

		public static final double SCHEDULE_WIDTH_RATIO = 0.2;

		public static final double BUTTON_WIDTH_RATIO = 0.2;
		public static final double BUTTON_HEIGHT_RATIO = 0.1;
	}
	
	public static final class GraphView {
        public static final double CIRCLE_RADIUS = 20;
        public static final double SCALE_BAR_LENGTH = 100;
        public static final double SCALE_BAR_PADDING = 15;
        public static final double SCALE_LABEL_PADDING = 20;
        public static final double SCALE_TICK_SIZE = 5;
        public static final double NODE_LABEL_FONT_SIZE = 14;
        public static final double ROUTE_SATURATION = 0.9;
        public static final double ROUTE_BRIGHTNESS = 0.9;
        public static final double KM_PER_PIXEL = 0.01;
    }
	
    
}
