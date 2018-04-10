package CarRace;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
/**
 * This class is a Controller for Car Races
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class Controller implements CarEvents {
	private final int MAXSPEED = 10;
	private final int NUM_OF_CARS = 5;
	private Stage stg;
	private Model model;
	private View view;

	public Controller(Model model, View view) {
		this.model = model;
		this.view = view;
	}
	public void setSpeedModelView() {
		Double speed;
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			speed = ((double) Math.random() * MAXSPEED)+4;
			model.changeSpeed(i, speed);
		}
	}
	public void setOwnerStage(Stage stg) {
		this.stg = stg;
	}

	public void errorAlert(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(stg);
		alert.setTitle("Error");
		alert.setContentText(msg);
		alert.show();
	}
}