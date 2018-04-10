package CarRace;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
/**
 * This class is a View for Car Races
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class View {
	private static final int NUM_OF_CARS = 5;
	private CarPane car_pane1, car_pane2 ,car_pane3, car_pane4 , car_pane5;
	private CarPane[] cars = {car_pane1, car_pane2 ,car_pane3, car_pane4 , car_pane5};
	private Model model;
	private GridPane cars_grid;
	//private ObservableList<String> items_color, items_car;
	private Button btn;
	private Color colors[] = {Color.RED, Color.AQUA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PINK
			, Color.VIOLET, Color.WHITE , Color.AQUAMARINE , Color.BLACK , Color.CHOCOLATE};

	public View() {
		createCarsGrid();
	}

	public void setModel(Model myModel) {
		model = myModel;
		if (model != null) {
			for(int i = 0 ; i < NUM_OF_CARS ; i++){
				int randomColor = (int)(Math.random()*colors.length);
				cars[i].setCarModel(model.getCarById(i));
				cars[i].setColor(colors[randomColor]);
				model.changeColor(i, colors[randomColor]);
			}
		}
	}

	public Model getModel() {
		return model;
	}

	public void createCarsGrid() {
		cars_grid = new GridPane();
		for(int i = 0 ; i < NUM_OF_CARS ; i++){	
			cars[i] = new CarPane();
			cars_grid.add(cars[i], 0, i);
		}
		cars_grid.setStyle("-fx-background-color: beige");
		cars_grid.setGridLinesVisible(true);

		ColumnConstraints column = new ColumnConstraints();
		column.setPercentWidth(100);
		cars_grid.getColumnConstraints().add(column);
		RowConstraints row = new RowConstraints();
		row.setPercentHeight(33);

		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars_grid.getRowConstraints().add(row);
		}
	}

	public void createAllTimelines() {
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars[i].createTimeline();
		}
	}
	public void createAllMoveTimelines(){
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars[i].createMoveTimeline();
		}
	}
	public void stopAllCars(){
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars[i].stop();
		}
	}

	public GridPane getCarsGrid() {
		return cars_grid;
	}

	public void setCarPanesMaxWidth(double newWidth) {
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars[i].setMaxWidth(newWidth);
		}
	}

	public CarPane getCarPane(int n) {
		if(n<0 || n > cars.length){
			return null;
		}
		return cars[n];
	}

	public Button getColorButton() {
		return btn;
	}
}
