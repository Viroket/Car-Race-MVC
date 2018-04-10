package CarRace;

import java.io.Serializable;

public class Bet implements Serializable {
	private double amount;
	private String clientName;
	private int carNumber;
	private int raceNumber;
	private int id;

	public Bet(float amount, String clientName, int carNumber, int raceNumber) {
		this.amount = amount;
		this.clientName = clientName;
		this.carNumber = carNumber;
		this.raceNumber = raceNumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRaceNumber() {
		return raceNumber;
	}

	public void setRaceNumber(int raceNumber) {
		this.raceNumber = raceNumber;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getClient() {
		return clientName;
	}

	public void setClient(String clientName) {
		this.clientName = clientName;
	}

	public int getCar() {
		return carNumber;
	}

	public void setCar(int carNumber) {
		this.carNumber = carNumber;
	}

	public String toString() {
		return String.format("Client Name : %s Amount: %.2f On Car Number : %d", this.clientName, this.amount,
				this.carNumber);
	}
}
