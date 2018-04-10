package CarRace;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
/**
 * This class provides a Server with JavaFx controlling races 
 * and handling clients
 * 
 * <br>
 * Extends: {@link Application}
 * 
 * @author Ofer Hod & Tal Hananel
 * @version 1.1
 * @since JDK 1.8
 */
public class CarRaceServer extends Application {
	private static final int NUM_OF_OPENING_RACES = 3;
	private static final double PRIZE_DEDUCTION = 0.05;
	private static final int MIN_BETS = 3;
	private TextArea ta = new TextArea();
	private ServerSocket serverSocket;
	private Socket socket;
	private static ObjectOutputStream activeRace = null;
	private static Connection con = null;
	private static String url = "jdbc:mysql://localhost/";
	private static String db = "";
	private static String driver = "com.mysql.jdbc.Driver";
	private static BufferedReader br;
	private static Statement st;
	private static int betCount = 0;
	private RandomAccessFile raf;
	private String carCounterFile = "betCount.dat";
	private Button btnNewRaces = new Button("New Race");
	private int raceID;
	private RandomAccessFile raf2;
	private String raceCounterFile = "raceCount.dat";
	private static HashMap<Integer, ObjectOutputStream> races = new HashMap<>();
	private static HashMap<Integer, Float> raceSums = new HashMap<>();
	private static HashMap<Integer, ArrayList<Bet>> raceBets = new HashMap<>();
	private static HashMap<Socket,ObjectOutputStream> clientList = new HashMap<Socket,ObjectOutputStream>();
	/**
	 * Main of <code>CarRaceServer</code> start the application
	 * 
	 * @param args
	 *            the command line arguments
	 * @see start
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		startDB();
		getBetNumber();
		ta.setEditable(false);
		VBox box = new VBox();
		HBox box2 = new HBox();
		box2.getChildren().addAll(btnNewRaces);
		box.getChildren().addAll(box2, new ScrollPane(ta));
		Scene scene = new Scene(box, 450, 200);
		primaryStage.setTitle("CarRaceServer"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		primaryStage.setAlwaysOnTop(true);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
					}
				Platform.exit();
				System.exit(0);
			}
		});
		btnNewRaces.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				increaseCount();
				getRaceNumber();
				CarRaceWindow cl = new CarRaceWindow(raceID);
				ta.appendText("New Race Created: "+raceID+"\n");
				cl.start(new Stage());
				insertRace(new Race(raceID));
			}
		});
		ta.appendText("MultiThreadServer started at " + new Date() + '\n');
		new Thread(() -> {
			try { // Create a server socket
				serverSocket = new ServerSocket(8000);
				while (true) { // Listen for a new connection request
					socket = serverSocket.accept();
					// Create and start a new thread for the connection
					ObjectInputStream stream = new ObjectInputStream(socket.getInputStream());
					ClientType.type type;
					type = (ClientType.type) stream.readObject();
					if(type == ClientType.type.User){
						new Thread(new HandleARaceClient(socket,stream)).start();
						clientList.put(socket,new ObjectOutputStream(socket.getOutputStream()));
					}
					else if(type == ClientType.type.Race){
						new Thread(new HandleARaceWindow(socket,stream)).start();
					}	
				}
			} catch (IOException ex) {
			} catch (ClassNotFoundException e) {
			}
		}).start();
		increaseCount();
		getRaceNumber();
		for (int i = 0; i < NUM_OF_OPENING_RACES; i++) {
			increaseCount();
			getRaceNumber();
			CarRaceWindow cl = new CarRaceWindow(raceID);
			cl.start(new Stage());
			ta.appendText("New Race Created: "+raceID+"\n");
			insertRace(new Race(raceID));
		}
	}
	/**
	 * This class provides a Thread for handling Server Clients 
	 * 
	 * <br>
	 * Extends: {@link Thread}
	 * 
	 * @author Ofer Hod & Tal Hananel
	 * @version 1.1
	 * @since JDK 1.8
	 */
	class HandleARaceClient extends Thread {
		private Socket connectToClient;
		private ObjectInputStream inStream;
		private Object message;

		public HandleARaceClient(Socket socket , ObjectInputStream inStream) {
			connectToClient = socket;
			this.inStream = inStream;
		}
		public void run() {
			try {
				while (true) {
					updateClients();
					if(activeRace ==null){
						activateRace();
					}
					if (inStream == null) {
						inStream = new ObjectInputStream(connectToClient.getInputStream());
					}
					if ((message = inStream.readObject()) != null) {
						if (message instanceof Bet) {
							if(raceBets.containsKey(((Bet) message).getRaceNumber())){
								raceBets.get(((Bet) message).getRaceNumber()).add((Bet) message);
							}else{
								ArrayList<Bet> bets = new ArrayList<Bet>();
								bets.add((Bet)message);
								raceBets.put(((Bet) message).getRaceNumber(),bets);
								
							}
							raceSums.put(((Bet) message).getRaceNumber(),sumBets(((Bet) message).getRaceNumber()));
							insertBet((Bet) message);
							if(activeRace == null){
								activateRace();
							}
						} else if (message instanceof Client) {
							insertClient((Client) message);
						} else if (message instanceof String){
							executeQuery((String)message,clientList.get(connectToClient));
						}
					}
				}
			} catch (SocketException ex) {
				try {
					serverSocket.close();
					socket.close();
				} catch (IOException e) {
				}
			} catch (IOException ex) {
			} catch (ClassNotFoundException e) {
			}
		}	
	}
	/**
	 * This class provides a Thread for handling Race Window Clients 
	 * 
	 * <br>
	 * Extends: {@link Thread}
	 * 
	 * @author Ofer Hod & Tal Hananel
	 * @version 1.1
	 * @since JDK 1.8
	 */
	class HandleARaceWindow extends Thread {
		private Socket connectToClient;
		private ObjectInputStream inStream;
		private Object message;

		public HandleARaceWindow(Socket socket ,ObjectInputStream inStream) {
			connectToClient = socket;
			this.inStream = inStream;
		}
		public void run() {
			try {
				
				while (true) {
					if (inStream == null) {
						inStream = new ObjectInputStream(connectToClient.getInputStream());
					}
					if ((message = inStream.readObject()) != null) {
						if (message instanceof Race) {
							activeRace = null;
							updateWhenRaceEnds((Race) message);
							activateRace();
						}else if(message instanceof Integer){
							races.put((Integer)message, new ObjectOutputStream(connectToClient.getOutputStream()));
							updateClients();
						}else if(message instanceof DeleteRaceMessage){
							int key = ((DeleteRaceMessage)message).getRaceID();
							races.remove(key);
							raceBets.remove(key);
							raceSums.remove(key);
							updateClients();
						}else if(message instanceof CarInfo){
							insertCar((CarInfo)message);
						}
					}
				}
			} catch (SocketException ex) {
				try {
					serverSocket.close();
					socket.close();
				} catch (IOException e) {
				}
			} catch (IOException ex) {
			} catch (ClassNotFoundException e) {
			}
		}
	}
	/**
	 * Starts up a DataBase Connection for the Server
	 * 
	 * @return void
	 */
	private void startDB() {
		try {
			Class.forName(driver);
			System.out.println("Driver Loaded");
			con = DriverManager.getConnection(url + db, "scott", "tiger");
			System.out.println("Connection Established");
			st = con.createStatement();
			con.setAutoCommit(false);
			FileInputStream fstream = new FileInputStream("CarRaceDB.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = "", strLine1 = "";
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine != null && !strLine.trim().equals("")) {
					if ((strLine.trim().indexOf("/*") < 0) && (strLine.trim().indexOf("*/") < 0)) {
						if (strLine.indexOf(';') >= 0) {
							strLine1 += strLine;
							System.out.println(strLine1);
							st.execute(strLine1);
							strLine1 = "";
						} else
							strLine1 += strLine;
					}
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (SQLException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	/**
	 * Inserts Bets into the Database
	 * 
	 * @param bet
	 *            An Object that contains Bet information
	 * @return void
	 */
	private synchronized void insertBet(Bet bet) {
		try {
			bet.setId(increaseBetCount());
			ta.appendText(String.format("New Bet Number %d Accepted for race : %d with amount : %f by : %s\n",
					bet.getId(),bet.getRaceNumber(),bet.getAmount(),bet.getClient()));
			st.execute(String.format("update Client set money = money-%f WHERE p_name = '%s';",
					bet.getAmount(),bet.getClient()));
			con.commit();
			st.execute(String.format("insert into Bet(b_id,r_id,amount,p_name,car) values(%d,%d, %f,'%s',%d);", betCount,
					bet.getRaceNumber(),bet.getAmount(), bet.getClient(), bet.getCar()));
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Inserts Clients into the Database
	 * 
	 *  @param client
	 *            An Object that contains client information
	 * @return void
	 */
	private synchronized void insertClient(Client client) {
		try {
			st.execute(
					String.format("insert into Client(p_name,money) values('%s',%f);", client.getName(), client.getEarnings()));
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Inserts Cars into the Database
	 * 
	 *  @param message
	 *            An Object that contains car information
	 * @return void
	 */
	private synchronized void insertCar(CarInfo message){
		try {
			st.execute(
					String.format("insert into Car(c_id,type,manufacturer,color,r_id) values (%d,'%s', '%s','%s',%d);"
							, message.getId(),message.getType(), message.getManufacturer(),message.getColor(),message.getRace()));
			con.commit();
		} catch (SQLException e) {
		}
	}
	/**
	 * Inserts Races into the Database
	 * 
	 *  @param race
	 *            An Object that contains race information
	 * @return void
	 */
	private synchronized void insertRace(Race race){
		try {
			st.execute(String.format("insert into Race(r_id,status,date,time,sum,winner) values (%d,'%s',null,null,null,null);",
					race.getRaceID(),"Pending"));
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Updates the Database when a race ends
	 * 
	 *  @param race
	 *            An Object that contains race information
	 * @return void
	 */
	private synchronized void updateWhenRaceEnds(Race race) {
		try {
			race.setBetSum(sumBets(race.getRaceID()));
			raceSums.remove(race.getRaceID());
			raceBets.remove(race.getRaceID());
			ArrayList<Client> winner = new ArrayList<>();
			ta.appendText(String.format(
					"Race number %d ended: winner is car number : %d , sum is : %f\n"
					+ " , Date : %s,time of race : %d%n",
					race.getRaceID(), race.getWinningCarNum(), race.getBetSum(), race.getDate(), race.getRaceTime()));
			ResultSet winners = st
					.executeQuery(String.format("select Client.p_name, Client.money from Client,Race,Bet where Client.p_name = Bet.p_name "
							+ "and Bet.r_id = Race.r_id and Bet.car = Race.winner and Race.r_id = %d;", race.getRaceID()));
			con.commit();
			while (winners.next()) {
				st.executeUpdate(String.format("update Client set money = %f where p_name = '%s';",
						(((race.getBetSum() - (race.getBetSum() * (PRIZE_DEDUCTION)/winner.size())))) + winners.getFloat(1), winners.getString(0)));
				con.commit();
			}
			st.execute(String.format("insert into System(r_id,profit,prize) values (%d,%f,%f);",
					race.getRaceID(),race.getBetSum() * PRIZE_DEDUCTION , (race.getBetSum() - (race.getBetSum() * PRIZE_DEDUCTION))));
			st.executeUpdate(String.format(
					"update Race set status = '%s' where r_id = %d;"
					,"Done", race.getRaceID()));
			st.executeUpdate(String.format(
					"update Race set date = '%s' where r_id = %d;"
					,race.getDate(),race.getRaceID()));
			st.executeUpdate(String.format(
					"update Race set time = %d where r_id = %d;"
					,race.getRaceTime(), race.getRaceID()));
			st.executeUpdate(String.format(
					"update Race set sum = %f where r_id = %d;"
					,race.getBetSum(), race.getRaceID()));
			st.executeUpdate(String.format(
					"update Race set winner = %d where r_id = %d;"
					,race.getWinningCarNum(), race.getRaceID()));
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Checks the Database for an existing client
	 * 
	 *  @param name
	 *            A String of the client name
	 * @return void
	 */
	public static boolean clientExists(String name) {
		try {
			if(st != null){
				ResultSet result = st.executeQuery("select Client.p_name from Client where Client.p_name = ';"+name+"';");
				con.commit();
				if(result != null){
					while (result.next()) {
						return true;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Updates the clients of the existing races
	 * 
	 * @return void
	 */
	private void updateClients(){
		for(ObjectOutputStream stream : clientList.values()){
			try {
				ArrayList<Integer> raceArray = new ArrayList<>();
				for(Integer race : races.keySet()){
					raceArray.add(race);
				}
				stream.writeObject(raceArray);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void getBetNumber() {
		try {
			if (raf == null) {
				raf = new RandomAccessFile(carCounterFile, "rws");
				raf.seek(0);
				try {
					betCount = raf.readInt();
				} catch (Exception e) {
					e.printStackTrace();
					raf.writeInt(0);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("finally")
	public synchronized int increaseBetCount() {
		try {
			raf.seek(0);
			betCount = raf.readInt();

			raf.seek(0);
			raf.writeInt(++betCount);
			return betCount;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			return betCount;
		}
	}

	private void getRaceNumber() {
		try {
			if (raf2 == null) {
				raf2 = new RandomAccessFile(raceCounterFile, "rws");
				raf2.seek(0);
				try {
					raceID = raf2.readInt();
				} catch (Exception e) {
					raf2.writeInt(0);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("finally")
	private synchronized int increaseCount() {
		try {
			raf2.seek(0);
			raceID = raf2.readInt();

			raf2.seek(0);
			raf2.writeInt(++raceID);
			return raceID;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			return raceID;
		}
	}
	/**
	 * Sums the bet amounts for a race
	 * 
	 *  @param race
	 *            An Object that contains race information
	 * @return void
	 */
	private float sumBets(int race) {
		float sum = 0;
		for (Bet bet : raceBets.get(race)) {
			if(bet.getRaceNumber() == race){
				sum += bet.getAmount();
			}	
		}
		return sum;
	}

	/**
	 * Queries the Database for user query input
	 * 
	 *  @param query
	 *            A String that holds the query to the database
	 *  @param stream
	 *            An ObjectOutputStream for the client that provided the query
	 *            to return results
	 * @return void
	 */
	private void executeQuery(String query,ObjectOutputStream stream){
		if(query.equals("")){
			return;
		}
		try {
			ArrayList<String> columns = new ArrayList<>(); 
			ArrayList<ArrayList<String>> list = new ArrayList<>();
			ResultSet result = st.executeQuery(query);
			con.commit();
			while(result.next()){
				ArrayList<String> inner = new ArrayList<>();
				for(int i = 1 ; i <= result.getMetaData().getColumnCount() ; i++)
					inner.add(result.getString(i));
				list.add(inner);
			}
			for(int i = 1 ;i<= result.getMetaData().getColumnCount();i++){
				columns.add(result.getMetaData().getColumnLabel(i));
			}
			int columnCount = result.getMetaData().getColumnCount();
			QueryMessage message = new QueryMessage(list,columnCount,columns);
			stream.writeObject(message);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error on Building Data");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Activates an idle race when it
	 * meets the criteria
	 * 
	 * @return void
	 */
	private void activateRace() {
		if(activeRace != null){
			return;
		}
		float max = -1;
		int selectedRace = -1;
		
		for(Integer race : raceSums.keySet()){
			if(raceBets.get(race).size() >= MIN_BETS){
				float sum = raceSums.get(race);
				if (max < sum) {
					max = sum;
					selectedRace = race;
				}
			}
		}
		if (selectedRace != -1) {
			try {
				activeRace = races.get(selectedRace);
				activeRace.writeObject(true);
				races.remove(selectedRace);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
