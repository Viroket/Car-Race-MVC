package CarRace;

import javafx.application.Application;
import javafx.scene.paint.Color;
/**
 * This class is a model for Car Races
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class Model {
	private static final int NUM_OF_CARS = 5;
	private int raceCounter;
	private Car[] cars = new Car[NUM_OF_CARS];
	
	private CarType.manufacturer[] manufacturers = CarType.manufacturer.values();
	private CarType.type[] types = CarType.type.values();

	public Model(int raceCounter) {
		int randomType = (int)(Math.random()*types.length);
		int randomManufacturer = (int)(Math.random()*manufacturers.length);
		this.raceCounter = raceCounter;
		for(int i = 0 ; i < NUM_OF_CARS ; i++){
			cars[i] = new Car(i, raceCounter,manufacturers[randomManufacturer],types[randomType],raceCounter);
		}
	}
	public void changeColor(int id, Color color) {
		getCarById(id).setColor(color);
	}

	public void changeRadius(int id, int radius) {
		getCarById(id).setRadius(radius);
	}

	public void changeSpeed(int id, double speed) {
		getCarById(id).setSpeed(speed);
	}

	public Car getCarById(int id) {
		if(id< 0 || id > cars.length){
			return null;
		}
		return cars[id];
	}

	public int getRaceCounter() {
		return raceCounter;
	}
}
