package CarRace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
/**
 * This class provides a client UI  with JavaFx for betting on races
 * 
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class CarRaceClient extends Application implements ClientType {
	private Stage stage;
	private static final int MAX_NAME_LENGTH = 8;
	private static final int MIN_NAME_LENGTH = 1;
	private static final int MIN_CAR = 1;
	private static final int MAX_CAR = 5;
	private ObjectOutputStream osToServer;
	private ObjectInputStream isFromServer;
	private String host = "localhost";
	private Socket socket;
	private ArrayList<Bet> bets = new ArrayList<>();
	private ArrayList<Integer> races = new ArrayList<>();
	private ListView<Integer> list = new ListView<>();
	private Button btnRegister = new Button("Register");
	private Button btnSubmit = new Button("Submit");
	private Button btnSignIn = new Button("Sign In");
	private TextField ta = new TextField();
	private TextField tfBet;
	private TextField tfCar;
	private TextField tfName;
	private HBox boxName;
	private HBox boxBet;
	private HBox boxCar;
	private String name;
	private Button btnQueries = new Button("Queries Window");
	private ComboBox<String> cboTableName = new ComboBox<>();
	private TableView<?> tableView = new TableView<Object>();
	private Button btShowContents = new Button("Show Contents");
	private Label lblStatus = new Label();
	private String queryString = "";
	
	/**
	 * Main of <code>CarRaceClient</code> start the application
	 * 
	 * @param args
	 *            the command line arguments
	 * @see start
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				Platform.exit();
			}
		});
		try {
			socket = new Socket(host, 8000);
			if (osToServer == null) {
				osToServer = new ObjectOutputStream(socket.getOutputStream());
			}
			osToServer.writeObject(ClientType.type.User);
		} catch (UnknownHostException e1) {
		} catch (IOException e1) {
		}
		list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				while (true) {
					try {
						Object message = new Object();
						if(isFromServer == null){
							isFromServer = new ObjectInputStream(socket.getInputStream());
						}
						message = isFromServer.readObject();
						if (message instanceof ArrayList<?>) {
							updateRaces((ArrayList<Integer>) message);
						} else if (message instanceof Stage) {
							((Stage) message).show();
						} else if (message instanceof QueryMessage){
							QueryMessage query = (QueryMessage)message;
							populateTableView(query.getSet(),query.getColumnCount(),query.getColumns(), tableView);
						}
					} catch (IOException ioe) {
						try {
							socket = new Socket(host, 8000);
						} catch (UnknownHostException e) {
							System.out.println("error on host");
						} catch (IOException e) {
						}
					} catch (ClassNotFoundException e) {
						System.out.println("class exception");
					}
				}

			}
		}).start();
		VBox pane = new VBox();
		boxName = new HBox();
		boxBet = new HBox();
		boxCar = new HBox();
		btnSubmit.setVisible(false);
		btnSubmit.setDisable(true);
		tfName = new TextField();
		boxName.getChildren()
				.addAll(new Label(
						String.format("%-26s", String.format("Enter Name(%d-%d):", MIN_NAME_LENGTH, MAX_NAME_LENGTH))),
						tfName, btnRegister, btnSignIn);
		cboTableName.getItems().addAll("Race History","Client History","CurrentState","System Statistics","Client Statistics");
		cboTableName.getSelectionModel().select(0);
		tfBet = new TextField();
		boxBet.getChildren().addAll(new Label(String.format("%-26s", "Enter Bet(Min 10):")), tfBet);
		boxBet.setVisible(false);
		tfCar = new TextField();
		boxCar.getChildren().addAll(
				new Label(String.format("%-22s", String.format("Enter Car Number(%d-%d):", MIN_CAR-1, MAX_CAR-1))), tfCar);
		boxCar.setVisible(false);
		ta.setEditable(false);
		initButtons();
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				btnSubmit.setDisable(false);
			}
		});
		pane.getChildren().addAll(boxName, boxBet, boxCar, btnSubmit,btnQueries, list, ta);
		Scene scene = new Scene(pane, 620, 650);
		stage.setScene(scene);
		stage.setTitle("New Bet");
		stage.setAlwaysOnTop(true);
		stage.show();
	}
	/**
	 * Updates the list of races displayed to the Client.
	 *
	 * @param arraylist
	 *            An ArrayList of integers that represent the race numbers
	 * @return void
	 */
	private void updateRaces(ArrayList<Integer> array){
		races.clear();
		races.addAll(array);
		Platform.runLater(() -> {
			if (races.isEmpty()) {
				list.setVisible(false);
				ta.setText("No races to bet on\n");
			} else {
				list.setItems(FXCollections.observableArrayList(races));
				list.getSelectionModel().select(0);
				list.setVisible(true);
			}
		});
	}
	/**
	 * Initializes the Buttons for the Client UI.
	 *
	 * @return void
	 */
	private void initButtons(){
		btnRegister.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String userName = tfName.getText();
				if (userName.length() > MAX_NAME_LENGTH || userName.length() < MIN_NAME_LENGTH) {
					ta.setText("username not in right length(1-8)");
				} else if (CarRaceServer.clientExists(userName)) {
					ta.setText("username already exists");
				} else {
					try {
						if (osToServer == null) {
							osToServer = new ObjectOutputStream(socket.getOutputStream());
						}
						ta.setText(String.format("Registered new user - %s", userName));
						tfName.setEditable(false);
						boxBet.setVisible(true);
						boxCar.setVisible(true);
						boxName.setVisible(false);
						btnSubmit.setVisible(true);
						Client client = new Client(userName);
						osToServer.writeObject(client);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btnSignIn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String userName = tfName.getText();
				if (userName.length() > MAX_NAME_LENGTH || userName.length() < MIN_NAME_LENGTH) {
					ta.setText("username not in right length(1-8)");
				} else if (CarRaceServer.clientExists(userName)) {
					ta.setText(String.format("Logged In as - %s", userName));
					tfName.setEditable(false);
					boxBet.setVisible(true);
					boxCar.setVisible(true);
					boxName.setVisible(false);
					btnSubmit.setVisible(true);
				} else {
					ta.setText("No such user - please register");
				}
			}
		});
		btnSubmit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					ta.setText("");
					name = tfName.getText();
					float amount = Float.parseFloat(tfBet.getText());
					int raceID = ((Integer)list.getSelectionModel().getSelectedItem());
					int carNum = Integer.parseInt(tfCar.getText());
					if (carNum > MAX_CAR-1 || carNum < MIN_CAR-1) {
						throw new Exception("Bad Input");
					}
					if (amount < 10) {
						throw new Exception("Bad Input");
					}
					if (osToServer == null) {
						osToServer = new ObjectOutputStream(socket.getOutputStream());
					}
					Bet bet = new Bet(amount, name, carNum, raceID);
					bets.add(bet);
					osToServer.writeObject(bet);
					ta.setText("New Bet: "+bet.toString());
				} catch (IOException e) {
					e.printStackTrace();
					ta.setText("No Connection to server!");
				} catch (Throwable e) {
					ta.setText("Invalid Input!");
				}
			}
		});
		btnQueries.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				startQueryWindow();
				
			}
		});
	}
	/**
	 * Opens a new Stage with a TableView for displaying
	 * Query Results.
	 * 
	 * @return void
	 */
	private void startQueryWindow(){
		Stage stage = new Stage();
		HBox hBox = new HBox(5);
		hBox.getChildren().addAll(new Label("Table Name"), cboTableName, btShowContents);
		hBox.setAlignment(Pos.CENTER);
		BorderPane pane = new BorderPane();
		pane.setCenter(tableView);
		pane.setTop(hBox);
		pane.setBottom(lblStatus);
		// Create a scene and place it in the stage
		Scene scene = new Scene(pane, 820, 480);
		stage.setTitle("DataBaseEx7"); // Set the stage title
		stage.setScene(scene); // Place the scene in the stage
		stage.setAlwaysOnTop(true);
		stage.show(); // Display the stage
		btShowContents.setOnAction(e -> showContents());
	}
	/**
	 * Sends the correct Query to the Server
	 * From the selected ComboBox index
	 * 
	 * @return void
	 */
	private void showContents() {
		try {
			switch(cboTableName.getSelectionModel().getSelectedItem()){
			case "Race History":
				queryString = showRaceHistory();
				break;
			case "Client History":
				getGamblesHistory();
				break;
			case "CurrentState":
				queryString = getRacesCurrentState();
				break;
			case "System Statistics":
				queryString = getSystemTable();
				break;
			case "Client Statistics":
				queryString = getGamblersProfit();
				break;
			}
			if (osToServer == null) {
				osToServer = new ObjectOutputStream(socket.getOutputStream());
			}
			osToServer.writeObject(queryString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Displays the information from the Query
	 * returned by the server
	 * 
	 * @param rs
	 *            An ArrayList of ArrayLists of Strings that contains the table information
	 * @param columnCount
	 *            An integer of the number of table columns
	 * @param columns
	 *            An ArrayList  of Strings that contains the table Headers
	 * @param tableView
	 *           A TableView to diplay the information to the user
	 * @return void
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateTableView(ArrayList<ArrayList<String>> rs,int  columnCount,ArrayList<String> columns, TableView tableView) {
		Platform.runLater(()-> {
			tableView.getColumns().clear();
			tableView.setItems(FXCollections.observableArrayList());
			try { 
				for(int i = 1 ; i <= columnCount ; i++)
				{
					final int columNum = i-1;
					TableColumn<String[],String> column = new TableColumn<>(columns.get(i-1));
					column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columNum]));
					tableView.getColumns().add(column);
				}
				for(ArrayList<String> array : rs)
				{
					String[] cells = new String[columnCount];
					for(String s : array)
						cells[array.indexOf(s)] = s;
					tableView.getItems().add(cells);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error on Building Data");
			}
		});
	}
	
	private String showRaceHistory() {
		return "select Race.* "
				+ "from Race "
				+ "where Race.status = '"+"Done"
				+ "' ;";
	}

	private String getRacesCurrentState() {
		return "select Race.status,Bet.* "
				+ "from Race, Bet "
				+ "where Race.r_id = Bet.r_id "
				+ "order by Race.r_id;";
	}

	private String getGamblesHistory() {
		return "select Race.r_id, Client.p_name, Race.time ,Bet.car,Bet.amount "
				+ "from Client, Bet, Race "
				+ "where Bet.r_id = Race.r_id and Client.p_name = Bet.p_name and Client.p_name = '"
				+ name + "' ;";
	}

	private String getSystemTable() {
		return "select System.r_id,System.profit,System.prize from System;";
	}

	private String getGamblersProfit() {
		return "select Client.p_name,Client.money from Client;";
	}
}
