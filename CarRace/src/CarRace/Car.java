package CarRace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

public class Car implements CarEvents {
	private int id;
	private int model_id;
	private double speed;
	private int wheelRadius;
	private Map<eventType, ArrayList<EventHandler<Event>>> carHashMap;
	private CarType.type type;
	private CarType.manufacturer manufacturer;
	private Color color;
	private int race;
	

	public Car(int id, int model_id, CarType.manufacturer manufacturer,CarType.type type,int race) {
		this.race = race;
		this.manufacturer = manufacturer;
		this.type = type;
		this.id = id;
		this.model_id = model_id;
		this.speed = 1;
		this.wheelRadius = 5;
		carHashMap = new HashMap<eventType, ArrayList<EventHandler<Event>>>();
		for (eventType et : eventType.values())
			carHashMap.put(et, new ArrayList<EventHandler<Event>>());
	}

	public int getId() {
		return id;
	}
	public CarInfo getInfo(){
		return new CarInfo(id,color,manufacturer,type,race);
	}
	public int getModelId() {
		return model_id;
	}

	public Color getColor() {
		return color;
	}

	public int getRadius() {
		return wheelRadius;
	}

	public double getSpeed() {
		return speed;
	}

	public int getRace() {
		return race;
	}

	public CarType.type getType() {
		return type;
	}

	public CarType.manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setColor(Color color) {
		this.color = color;
		processEvent(eventType.COLOR, new ActionEvent());
	}

	public void setSpeed(double speed) {
		this.speed = speed;
		processEvent(eventType.SPEED, new ActionEvent());
	}

	public void setRadius(int wheelRadius) {
		this.wheelRadius = wheelRadius;
		processEvent(eventType.RADIUS, new ActionEvent());
	}

	public synchronized void addEventHandler(EventHandler<Event> l, eventType et) {
		ArrayList<EventHandler<Event>> al;
		al = carHashMap.get(et);
		if (al == null)
			al = new ArrayList<EventHandler<Event>>();
		al.add(l);
		carHashMap.put(et, al);
	}

	public synchronized void removeEventHandler(EventHandler<Event> l, eventType et) {
		ArrayList<EventHandler<Event>> al;
		al = carHashMap.get(et);
		if (al != null && al.contains(l))
			al.remove(l);
		carHashMap.put(et, al);
	}

	private void processEvent(eventType et, Event e) {
		ArrayList<EventHandler<Event>> al;
		synchronized (this) {
			al = carHashMap.get(et);
			if (al == null)
				return;
		}
		for (int i = 0; i < al.size(); i++) {
			EventHandler<Event> handler = (EventHandler<Event>) al.get(i);
			handler.handle(e);
		}
	}
}