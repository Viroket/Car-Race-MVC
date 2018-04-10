package CarRace;

import java.io.Serializable;

import javafx.scene.paint.Color;

public class CarInfo implements Serializable{
	private static final long serialVersionUID = -7590262415515515206L;
	private CarType.type type;
	private CarType.manufacturer manufacturer;
	private String color;
	private int race;
	private int id;
	
	public CarInfo(int id,Color color ,CarRace.CarType.manufacturer manufacturer, CarRace.CarType.type type, int race) {
		this.id = id;
		this.manufacturer = manufacturer;
		this.type = type;
		this.race = race;
		this.color = color.toString();
	}

	public CarType.type getType() {
		return type;
	}

	public void setType(CarType.type type) {
		this.type = type;
	}

	public CarType.manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(CarType.manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color.toString();
	}

	public int getRace() {
		return race;
	}

	public void setRace(int race) {
		this.race = race;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
