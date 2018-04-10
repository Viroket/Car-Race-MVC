package CarRace;

import java.io.Serializable;

public class DeleteRaceMessage implements Serializable{
	private int raceID;
	
	public DeleteRaceMessage(int raceID){
		this.raceID = raceID;
	}
	
	public int getRaceID(){
		return raceID;
	}
}
