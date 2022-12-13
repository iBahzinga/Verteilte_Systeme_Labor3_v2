
public class Definition {
	
	// Define value for Packet class and ConnectionSocket class
	public static final int FRAME_SIZE = 34;
	public static final int DATA_SIZE = 24;
//	public static final int TIMESTAMP_SIZE = 7;
//	public static final int STATIONCLASS_SIZE = 1;
//	public static final int SLOTNUMBER_SIZE = 1;
	
	public static final int DATA_INDEX = 1;
	public static final int SLOTNUMBER_INDEX = 25;
	public static final int TIMESTAMP_INDEX = 26;
	public static final int STATIONCLASS_INDEX = 0;
	
	
	// Define Enum for Station class
	public enum AccessMode {
		GET, ADD, CLEAR
	}
	public enum StationClass {
		A, B
	}
//	public enum StationState {
//		LISTENING, SENDING
//	}
	
	// Define Value for Station class
	public static final int SLOT_SIZE = 40;
	public static final int HALF_SLOT_SIZE = 20;
	public static final int FOURTH_SLOT_SIZE = 10;
//	public static final int EIGHT_SLOT_SIZE = 5;
//	public static final double ONE_MILISEC_OFFSET = 1000.0;

	
	// Define value for running a station (main class)
	public static final String ETHERNETNAME = "eth2";
	public static final String IP_ADDRESS = "225.10.1.2";
	public static final String PORT = "12345";
	public static final String A_CLASS = "A";
	public static final String B_CLASS = "B";
	public static final String UTC_OFFSET = "20";
}
