package CarRace;

import java.io.Serializable;

public class Client implements Serializable{
	private static final long serialVersionUID = -2634988867319775237L;
	private String name;
	private double earnings;
	
	public Client(String name){
		this.name = name;
		this.earnings = 0;
	}
	public Client(String name,double earnings){
		this.name = name;
		this.earnings = earnings;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getEarnings() {
		return earnings;
	}
	public void setEarnings(double money) {
		this.earnings = money;
	}
}
