package CarRace;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
/**
 * This class provides a UI  with JavaFx for displaying races
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class CarRaceWindow extends Application implements ClientType {
	private static final int MINUTE = 60000;
	private static final int MAX_CAR = 5;
	private static final int TIME_OF_SECOND = 1000;
	private static final int TIME_TO_SWITCH_CAR_SPEED = 30000;
	private ObjectOutputStream osToServer;
	private ObjectInputStream isFromServer;
	private String host = "localhost";
	private Socket socket;
	private TextArea taRace = new TextArea();
	private View view = new View();
	private Controller controller;
	private Date raceDate = new Date();
	private boolean startRace = false;
	private boolean isRaceOver = false;
	private int raceID;
	private int startRaceTimer = 5;
	private int winningCar;
	private Stage stage;
	private long raceTime;
	private MediaPlayer mp;
	private String songs[] = {"1.mp3","2.mp3","3.mp3"};
	private int songLengths[] = {372,242,99};
	private double songSelected = Math.random()*songs.length;

	public CarRaceWindow(int raceID) {
		this.raceID = raceID;
	}

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				if(mp != null){
					mp.pause();
					mp.stop();
				}
				Platform.exit();
			}
		});
		stage.setResizable(false);
		taRace.appendText("Race will not start until at least 3 bets are accepted\n");
		try {
			socket = new Socket(host, 8000);
			if (osToServer == null) {
				osToServer = new ObjectOutputStream(socket.getOutputStream());
			}
			osToServer.writeObject(ClientType.type.Race);
			osToServer.writeObject(raceID);
		} catch (UnknownHostException e1) {
		} catch (IOException e1) {
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!startRace) {
					try {
						Object message = new Object();
						if (isFromServer == null) {
							isFromServer = new ObjectInputStream(socket.getInputStream());
						}
						message = isFromServer.readObject();
						if (message instanceof Boolean) {
							startRace = (Boolean) message;
							if (startRace == true) {
								taRace.appendText("Starting Race..\n");
								startRace();
							}
						}
					} catch (IOException ioe) {
						try {
							socket = new Socket(host, 8000);
						} catch (UnknownHostException e) {
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (ClassNotFoundException e) {
					}
				}

			}
		}).start();
		VBox box = new VBox();
		Model model = new Model(raceID);
		controller = new Controller(model, view);
		view.setModel(model);
		taRace.setEditable(false);
		
		
		try {
			if (osToServer == null) {
				osToServer = new ObjectOutputStream(socket.getOutputStream());
			}
			for(int i = 0 ; i < MAX_CAR ; i++){
				osToServer.writeObject(model.getCarById(i).getInfo());
			}
		} catch (IOException e1) {
		}

		SubScene sub = new SubScene(view.getCarsGrid(), 850, 350);
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setTranslateZ(-680);
		camera.setTranslateY(175);
		camera.setTranslateX(430);
		camera.setNearClip(0.1);
		camera.setFarClip(3000.0);
		camera.setFieldOfView(30);
		sub.setCamera(camera);

		box.getChildren().addAll(sub, taRace);
		Scene scene = new Scene(box, 850, 650);
		controller.setOwnerStage(stage);
		stage.setScene(scene);
		stage.setTitle("CarRaceView" + raceID);
		stage.setAlwaysOnTop(true);
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) { // stub
				view.setCarPanesMaxWidth(newValue.doubleValue());
			}
		});
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					if (osToServer == null) {
						osToServer = new ObjectOutputStream(socket.getOutputStream());
					}
					osToServer.writeObject(new DeleteRaceMessage(raceID));
				} catch (IOException e) {
				}
			}
		});
		view.createAllTimelines();
		controller.setSpeedModelView();
		stage.show();
	}
	/**
	 * finds the Winning Car from a Race
	 * 
	 * @return void
	 */
	private void findWinningCar() {
		double max = 0;
		for (int i = 0; i < MAX_CAR; i++) {
			if (max < view.getCarPane(i).getEndX()) {
				max = view.getCarPane(i).getEndX();
			}
		}
		for (int i = 0; i < MAX_CAR; i++) {
			if (max == view.getCarPane(i).getEndX()) {
				winningCar = i;
			}
		}
	}
	/**
	 * Stops the Race and sends its information to the Server
	 * 
	 * @return void
	 */
	private void endRace() {
		findWinningCar();
		taRace.appendText(String.format("Race Has Ended! The Winner Is Car Number : %d\n", winningCar));
		taRace.appendText("Screen will close in 1 minute..\n");
		try {
			if (osToServer == null) {
				osToServer = new ObjectOutputStream(socket.getOutputStream());
			}
			Race race = new Race(this.raceID, raceTime, winningCar, raceDate);
			osToServer.writeObject(race);
		} catch (IOException e) {
		} catch (Throwable e) {
		}
	}
	/**
	 * Starts the Race
	 * 
	 * @return void
	 */
	private void startRace() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				openingSound();
				while (startRaceTimer > 0) {
					try {
						Thread.sleep(TIME_OF_SECOND);
						Platform.runLater(() -> {
							taRace.appendText(startRaceTimer + "\n");
							startRaceTimer--;
						});
					} catch (InterruptedException e) {
					}
				}
				Platform.runLater(() -> {
					playMusic();
					taRace.appendText("Race Has Started!\n");
					view.createAllMoveTimelines();
					controller.setSpeedModelView();
				});
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (!isRaceOver) {
							try {
								Thread.sleep(TIME_TO_SWITCH_CAR_SPEED);
							} catch (InterruptedException e) {
							}
							Platform.runLater(() -> {
								controller.setSpeedModelView();
							});
						}
					}
				}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(songLengths[(int)songSelected]*TIME_OF_SECOND+2*TIME_OF_SECOND);
							raceTime = songLengths[(int)songSelected]*TIME_OF_SECOND+2*TIME_OF_SECOND;
						} catch (InterruptedException e) {
						}
						Platform.runLater(() -> {
							view.stopAllCars();
							endRace();
							closingSound();
						});
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(MINUTE / 2);
									taRace.appendText("Closing in 30 seconds..\n");
									Thread.sleep((MINUTE / 2) - 10 * TIME_OF_SECOND);
									for (int i = 10; i > 0; i--) {
										Thread.sleep(TIME_OF_SECOND);
										taRace.appendText(i + "..");
									}
									Thread.sleep(TIME_OF_SECOND);
								} catch (InterruptedException e) {
								}
								Platform.runLater(() -> {
									stage.close();
								});
							}
						}).start();
					}
				}).start();
			}
		}).start();
	}
	/**
	 * returns the PrimaryStage of the Race
	 * 
	 * @return Stage
	 */
	public Stage getStage() {
		return stage;
	}
	/**
	 * Plays Music while the race is Active
	 * 
	 * @return void
	 */
	public void playMusic(){
		mp = new MediaPlayer(new Media(new File((songs[2])).toURI().toString()));
		mp.play();
	}
	/**
	 * Plays a Count down to the race start
	 * 
	 * @return void
	 */
	public void openingSound(){
		Media musicFile = new Media(new File(("Countdown.mp3")).toURI().toString());
		mp = new MediaPlayer(musicFile);
		mp.play();
	}
	/**
	 * Plays a Cheering sound at the end of the race
	 * 
	 * @return void
	 */
	public void closingSound() { 
		Media musicFile = new Media(new File(("Cheering.mp3")).toURI().toString());
		mp = new MediaPlayer(musicFile);
		mp.play();
	}
}