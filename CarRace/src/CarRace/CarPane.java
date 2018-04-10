package CarRace;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
/**
 * This class provides a UI  with JavaFx for displaying a 3D car
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class CarPane extends Pane implements CarEvents,CarType {
	final int MOVE = 1;
	final int STOP = 0;
	private double endX;
	private double xCoor , rotate = 0;
	private double yCoor;
	private double zCoor;
	private Timeline tl; // speed=setRate()
	private Color color;
	private int r;// radius
	private Car car;
	private double shade = 0.7;
	private PhongMaterial redMaterial = new PhongMaterial();
	private Sphere sphere = new Sphere();
	private Sphere sphere2 = new Sphere();
	private Sphere sphere3 = new Sphere();
	private Sphere sphere4 = new Sphere();
	private Box myBox;
	private Cylinder myCylinder;
	
	class SpeedEvent implements EventHandler<Event> {

		@Override
		public void handle(Event event) {
			setSpeed(car.getSpeed());
		}
	}

	public CarPane() {
		this.color = Color.RED;
		xCoor = 0;
		endX = 0;
		r = 5;
		int random = (int)((Math.random()*20)+20);
		myBox = new Box(40, random, 20);
		myCylinder = new Cylinder(12, random+10);
	}

	public void setCarModel(Car myCar) {
		car = myCar;
		if (car != null) {
			car.addEventHandler(new SpeedEvent(), eventType.SPEED);
		}
	}

	public Car getCarModel() {
		return car;
	}

	public void moveCar(int n) {
		endX +=n;
		yCoor = getHeight();
		setMinSize(10 * r, 6 * r);
		if (xCoor > getWidth()) {
			xCoor = -10 * r;
			shade = 0.7;
		} 
		if(xCoor > getWidth()) {
			rotate =  0;
		}
		
		else {
			rotate  += n;
			shade = shade + 0.0009;
			xCoor += n;
		}
		//color for the wheels
		redMaterial.setDiffuseColor(Color.DARKGRAY);
		redMaterial.setSpecularColor(Color.GRAY);
		
		//random color for the car
		PhongMaterial redMaterial2 = new PhongMaterial();
		redMaterial2.setDiffuseColor(color);
		redMaterial2.setSpecularColor(color);
		
		// Draw the car
		sphere.setRadius(7.0);
		sphere.setTranslateX(xCoor + r * 3);
		sphere.setTranslateY(yCoor - r - 4);
		sphere.setTranslateZ(1);
		sphere.setMaterial(redMaterial);

		
		sphere2.setRadius(7.0);
		sphere2.setTranslateX(xCoor - 4.5 + r * 3);
		sphere2.setTranslateY(yCoor - r - 4);
		sphere2.setTranslateZ(-10);
		sphere2.setMaterial(redMaterial);

		
		sphere3.setRadius(7.0);
		sphere3.setTranslateX(xCoor + r * 7);
		sphere3.setTranslateY(yCoor - r - 4);
		sphere3.setTranslateZ(1);
		sphere3.setMaterial(redMaterial);

		
		sphere4.setRadius(7.0);
		sphere4.setTranslateX(xCoor - 4.5 + r * 7);
		sphere4.setTranslateY(yCoor - r - 4);
		sphere4.setTranslateZ(-10);
		sphere4.setMaterial(redMaterial);

		
		myBox.setTranslateX(xCoor + 20);
		myBox.setTranslateY(yCoor - 4.1 * r);
		myBox.setTranslateZ(8);
		Rotate rx3 = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.X_AXIS);
		Rotate ry3 = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Y_AXIS);
		Rotate rz3 = new Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Z_AXIS);
		myBox.getTransforms().addAll(rx3 , ry3 , rz3);
		
		myCylinder.setTranslateX(xCoor + 20);
		myCylinder.setTranslateY(yCoor - myBox.getHeight());
		myCylinder.setTranslateZ(14);
		
		myBox.setMaterial(redMaterial2);
		myCylinder.setMaterial(redMaterial2);
		
		Group gr = new Group();
		gr.getChildren().addAll(myCylinder ,myBox);
		
		Group grp = new Group();
		grp.getChildren().addAll(sphere, sphere3,gr,sphere4, sphere2 );
		grp.setDepthTest(DepthTest.ENABLE);
		
		getChildren().clear();
		getChildren().add(grp);
	}

	public void createTimeline() {
		EventHandler<ActionEvent> eventHandler = e -> {
			moveCar(STOP); // move car pane according to limits
		};
		tl = new Timeline();
		tl.setCycleCount(Timeline.INDEFINITE);
		KeyFrame kf = new KeyFrame(Duration.millis(100), eventHandler);
		tl.getKeyFrames().add(kf);
		tl.play();
	}
	public void createMoveTimeline() {
		EventHandler<ActionEvent> eventHandler = e -> {
			moveCar(MOVE); // move car pane according to limits
		};
		tl = new Timeline();
		tl.setCycleCount(Timeline.INDEFINITE);
		KeyFrame kf = new KeyFrame(Duration.millis(100), eventHandler);
		tl.getKeyFrames().add(kf);
		tl.play();
	}
	public void stop() {
		EventHandler<ActionEvent> eventHandler = e -> {
			moveCar(STOP); // move car pane according to limits
		};
		tl.getKeyFrames().clear();
		KeyFrame kf = new KeyFrame(Duration.millis(100), eventHandler);
		tl.getKeyFrames().add(kf);
		tl.stop();
	}

	public Timeline getTimeline() {
		return tl;
	}

	public void setSpeed(double speed) {
		if (speed == STOP) {
			tl.stop();
		} else {
			tl.setRate(speed);
			tl.play();
		}
	}

	public double getX() {
		return xCoor;
	}

	public double getY() {
		return yCoor;
	}

	public double getEndX() {
		return endX;
	}
	public Color getColor(){
		return color;
	}
	public void setColor(Color color){
		this.color = color;
	}
}