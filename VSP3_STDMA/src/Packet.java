public class Packet {

	public Definition.StationClass stationClass;

	public byte[] data;
	public byte slotNumber;
	public long timeStamp;
	public long arrivalTime;
	
	public Packet copy() {
		Packet copy = new Packet();
		copy.stationClass = this.stationClass;
		copy.data = this.data;
		copy.slotNumber = this.slotNumber;
		copy.timeStamp = this.timeStamp;
		copy.arrivalTime = this.arrivalTime;
		
		return copy;
	}

	public static byte[] unpack(Packet msg) {

		byte[] result = new byte[Definition.FRAME_SIZE];

		// Byte 0 : Station Class
		if(msg.stationClass == Definition.StationClass.A) {
			result[Definition.STATIONCLASS_INDEX] = 'A';
		} else {
			result[Definition.STATIONCLASS_INDEX] = 'B';
		}

		// Byte 1-24 : Data

		for (int i = 0; i < msg.data.length; i++) {
			result[i + Definition.DATA_INDEX] = msg.data[i];
		}

		// Byte 25 : slot number

		result[Definition.SLOTNUMBER_INDEX] = (byte) (msg.slotNumber + 1);

		// Byte 26 - 33 : timestamp

		byte[] timestamp = convertLongToByte(msg.timeStamp);

		for (int i = 0; i < timestamp.length; i++) {
			result[i + Definition.TIMESTAMP_INDEX] = timestamp[i];
		}

		return result;
	}


	public static Packet pack(byte[] msg, long arivalTime) {

		Packet result = new Packet();

		// Byte 0 : Station Class
		if(msg[Definition.STATIONCLASS_INDEX] == 'A') {
			result.stationClass = Definition.StationClass.A;
		} else {
			result.stationClass = Definition.StationClass.B;
		}

		// Byte 1-24 : Data
		byte[] data = new byte[Definition.DATA_SIZE];

		for (int i = Definition.DATA_INDEX; i <= Definition.DATA_SIZE; i++) {
			data[i - Definition.DATA_INDEX] = msg[i];
		}

		// Byte 25 : slot number
		result.slotNumber = (byte) (msg[Definition.SLOTNUMBER_INDEX] - 1);

		// Byte 26 - 33 : timestamp
		byte[] timestampbyte = new byte[8];
		for (int i = Definition.TIMESTAMP_INDEX; i < Definition.FRAME_SIZE; i++)
		{
			timestampbyte[i - Definition.TIMESTAMP_INDEX] = msg[i];
		}

		result.timeStamp = convertByteToLong(timestampbyte);


		result.arrivalTime = arivalTime;
		return result;
	}
	
	// TODO: For testing purpose, can it be deleted?
	public void verbose()
	{
		System.out.println("------------------MESSAGE------------------");
		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		System.out.println("Station Class:   " + (stationClass == Definition.StationClass.A ? 'A' : 'B'));
		System.out.print("Data: ");
		for (int i = 0; i < this.data.length; i++)
		{
			System.out.print((char) data[i]);
		}
		System.out.println();
		System.out.flush();

		System.out.println("Slot number: " + this.slotNumber);
		System.out.println("Timestamp: " + this.timeStamp);
		System.out.println("Time of Arrival: " + this.arrivalTime);

		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		System.out.println("-----------------/MESSAGE------------------");
	}

	
	////////////////////////////////Hilfmethode///////////////////////////////////////////

	private static long convertByteToLong(byte[] b) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;

	}
	

	private static byte[] convertLongToByte(long l) {
		byte[] result = new byte[8];
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}

}
