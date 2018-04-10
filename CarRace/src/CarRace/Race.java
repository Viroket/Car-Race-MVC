package CarRace;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Race implements Serializable{
	private int raceID;
	private double betSum;
	private long raceTime;
	private int WinningCarNum;
	private String date;
	private ArrayList<Bet> bets;
	
	public Race(int raceID){
		this.raceID = raceID;
	}
	public Race(int raceID , long raceTime , int WinningCarNum , Date date){
		this.raceID = raceID;
		this.raceTime = raceTime;
		this.WinningCarNum = WinningCarNum;
		this.date = new SimpleDateFormat("yyyy-MM-dd").format(date);
		bets = new ArrayList<>();
	}
	
	public ArrayList<Bet> getBets() {
		return bets;
	}

	public void setBets(ArrayList<Bet> bets) {
		this.bets = bets;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getRaceID() {
		return raceID;
	}
	public void setRaceID(int raceID) {
		this.raceID = raceID;
	}
	public double getBetSum() {
		return betSum;
	}
	public void setBetSum(int betSum) {
		this.betSum = betSum;
	}
	public void setBetSum(double betSum) {
		this.betSum = betSum;
	}
	public long getRaceTime() {
		return raceTime;
	}
	public void setRaceTime(long raceTime) {
		this.raceTime = raceTime;
	}
	public int getWinningCarNum() {
		return WinningCarNum;
	}
	public void setWinningCarNum(int winningCarNum) {
		WinningCarNum = winningCarNum;
	}
}
