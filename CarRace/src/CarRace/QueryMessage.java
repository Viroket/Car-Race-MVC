package CarRace;

import java.io.Serializable;
import java.util.ArrayList;

public class QueryMessage implements Serializable{
	private static final long serialVersionUID = 169122819665489939L;
	private ArrayList<ArrayList<String>> set;
	private int columnCount;
	private ArrayList<String> columns;
	
	@SuppressWarnings("unchecked")
	public QueryMessage(ArrayList<ArrayList<String>> set,int columnCount,ArrayList<String> columns){
		this.set = set;
		this.columnCount = columnCount;
		this.columns = (ArrayList<String>) columns.clone();
	}
	public ArrayList<ArrayList<String>> getSet(){
		return set;
	}
	public int getColumnCount(){
		return columnCount;
	}
	public ArrayList<String> getColumns(){
		return columns;
	}
}
