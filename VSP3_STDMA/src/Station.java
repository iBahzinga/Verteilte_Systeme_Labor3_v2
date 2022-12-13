import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Station {
	private static Definition.StationClass stationClass;
	private static long utcOffset;
	private static MulticastHandler socket;
	private static PacketList thisFramePackets;
	private static PacketList lastFramePackets;
	private static long syncOffset = 0;
	private static byte slot;
	private static char[] data = new char[Definition.DATA_SIZE];
	private static ArrayList<Byte> occupiedSlots = new ArrayList<Byte>();

	private static Reader dataReader;
	private static Receiver dataReceiver;
	private static Sender dataSender;
	private static Evaluator dataEvaluator;

	public static void main(String[] args) {

		if (args.length < 5) {
			System.err.println("Not enough arguments");
			return;
		}

		String ethernetName = args[0];
		String ipAddress = args[1];
		int port = Integer.parseInt(args[2]);

		switch (args[3]) {
			case "A":
				stationClass = Definition.StationClass.A;
				break;
			case "B":
				stationClass = Definition.StationClass.B;
				break;
			default:
				System.err.println("Station Class is not defined");
				return;
		}
		utcOffset = Long.parseLong(args[4]);
		socket = new MulticastHandler(port, ipAddress, ethernetName);
		if (!socket.init()) {
			System.err.println("Socket can not init");
			return;
		}

		thisFramePackets = new PacketList();
		lastFramePackets = new PacketList();

		thisFramePackets.packetList = new ArrayList<>();
		lastFramePackets.packetList = new ArrayList<>();

		dataReader = new Reader();

		dataEvaluator = new Evaluator();

		dataReceiver = new Receiver();
		dataSender = new Sender();

		dataEvaluator.start();
		dataSender.start();
		dataReceiver.start();
		dataReader.start();

	}

	public synchronized static PacketList accessMessageList(Definition.AccessMode am, Packet msg) {
		switch (am) {
		case ADD:
			thisFramePackets.packetList.add(msg);
			return null;
		case GET:
			return thisFramePackets.copy();
		case CLEAR:
			thisFramePackets.packetList.clear();
			return null;
		default:
			System.err.println("No Definition of Accessmode");
			return null;
		}

	}

	private static void initialNewFrame() {
		while (true) {
			long currentTime = System.currentTimeMillis() + utcOffset + syncOffset;
			if (currentTime % 1000 == 0) {
				accessMessageList(Definition.AccessMode.CLEAR, null);
				break;
			}
		}
	}

	private static class Sender extends Thread implements Runnable {
		@Override
		public void run() {
			initialNewFrame();
			long oldFrameNumber = 0;
			long currentTime = System.currentTimeMillis() + utcOffset + syncOffset;;
			long currentFrameNumber = (long) (((double) (currentTime)) / 1000.0);

			waitToJoin(currentTime, currentFrameNumber);

			while (true) {
				currentTime = System.currentTimeMillis() + utcOffset + syncOffset;
				currentFrameNumber = (long) (((double) (currentTime)) / 1000.0);
				int slotBegin = (slot * Definition.SLOT_SIZE);
				int sendBeginTime = (slotBegin + (Definition.HALF_SLOT_SIZE));
				int sendEndTime = sendBeginTime + Definition.FOURTH_SLOT_SIZE;

				if (currentTime % 1000 >= sendBeginTime && currentTime%1000 <= sendEndTime && oldFrameNumber!=currentFrameNumber) {
					if ((currentFrameNumber != oldFrameNumber + 1)) {
						System.err.println("[DEBUG] FRAMES MISSED!");
					}
					oldFrameNumber = currentFrameNumber;
					// Read data from buffer to send
					byte[] newData = null;
					synchronized (data) {
						newData = new byte[Definition.DATA_SIZE];
						for (int i = 0; i < Definition.DATA_SIZE; i++) {
							newData[i] = (byte) data[i];
						}
					}

					byte currentSlot = slot;
					byte nextSlot = getSlot();

					if (nextSlot >= 0) {
						slot = nextSlot;

						//0 - 24
						int slotsTillFrameEnd = 24 - currentSlot;
						int slotLengthsToWait = slotsTillFrameEnd + nextSlot + 1;
						int diffInMS = slotLengthsToWait * Definition.SLOT_SIZE - 1;

						Packet newMessage = new Packet();
						newMessage.stationClass = stationClass;
						newMessage.data = newData;
						newMessage.slotNumber = slot;
						newMessage.timeStamp = currentTime; // Maybe get new time

						byte[] newByteMessage = Packet.unpack(newMessage);

						socket.send(newByteMessage);

//						long cart = System.currentTimeMillis();
//						System.out.println("IT TOOK " + (cart - curt) + " MS");

						try {
							Thread.sleep(diffInMS);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.err.println("[DEBUG] NO FREE SLOTS");
					}
				}
			}
		}
		private static byte getSlot(){

			ArrayList<Byte> freeSlots = new ArrayList<Byte>();
			for (byte i = 0; i <= 24; i++) {
				freeSlots.add(i);
			}
			//System.err.println("[DEBUG] occupiedSlots: " + occupiedSlots.size());
			synchronized (occupiedSlots) {
				for (Byte i : occupiedSlots) {
					freeSlots.remove((Object) i);
				}
			}
			//System.err.println("[DEBUG] freeSlots: " + freeSlots.size());
			int randomSlot = 0;
			if (freeSlots.size() > 0){
				randomSlot = (int) (Math.random() * (double) (freeSlots.size()));
				return freeSlots.get(randomSlot);
			}else{
				return -1;
			}
		}
		private static void waitToJoin( long currentTime, long currentFrameNumber){
			long startFrameNumber = (long) ((((double) (currentTime)) / 1000.0) + 3);
			while (currentFrameNumber != startFrameNumber){
				currentTime = System.currentTimeMillis() + utcOffset + syncOffset;
				currentFrameNumber = (long) (((double) (currentTime)) / 1000.0);
			}
			try {
				Thread.sleep(990);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			slot = getSlot();
		}
	}
	private static class Reader extends Thread implements Runnable {
		@Override
		public void run()
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			while (true)
			{
				try
				{
					char[] newData = new char[Definition.DATA_SIZE];
					while (reader.read(newData) != Definition.DATA_SIZE);

					synchronized (data)
					{
						data = newData;
					}
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	private static class Receiver extends Thread implements Runnable {
		@Override
		public void run()
		{
			initialNewFrame();

			while (true)
			{
				byte[] buf = new byte[34];
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				socket.receive(p);
				long arrivalTime = System.currentTimeMillis() + utcOffset + syncOffset;

				Packet newMessage = new Packet();
				newMessage = Packet.pack(buf, arrivalTime);
				accessMessageList(Definition.AccessMode.ADD, newMessage);

				synchronized (occupiedSlots)
				{
					occupiedSlots.add(newMessage.slotNumber);
				}
			}
		}
	}
	private static class Evaluator extends Thread implements Runnable {
		@Override
		public void run() {
			initialNewFrame();

			long frameNumber = 0;
			while (true) {
				long currentTime = System.currentTimeMillis() + utcOffset + syncOffset;
				long currentFrameNumber = (long) (((double) (currentTime)) / 1000.0);

				if (currentTime % 1000 <= Definition.FOURTH_SLOT_SIZE && frameNumber != currentFrameNumber) {
					if ((frameNumber != 0) && (currentFrameNumber != frameNumber + 1)) {
						System.err.println("[DEBUG] FRAMES MISSED!");
					}
					frameNumber = currentFrameNumber;

					// Handle the last frame that occured (thisFrame)
					// thisFrame is the frame we are CURRENTLY EVALUATING
					// lastFrame is the last frame we ALREADY HANDLED
					PacketList currentFrame = (PacketList) accessMessageList(Definition.AccessMode.GET, null);
					PacketList previousFrame = lastFramePackets;

					int diffsum = 0;
					int msgNumber = 0;
					// Evauluate in which slots other stations have sent something
					for (Packet p : currentFrame.packetList)
					{

						long arrTime = p.arrivalTime;
						long frameBegin = currentTime - 1000;

						int millisAfterFrameStart = (int) (arrTime - frameBegin);

						int actualSlot = (int) Math.ceil(millisAfterFrameStart / Definition.SLOT_SIZE);

						// Differenz Timestamp - TimeOfArrival für diese Message
						if (p.stationClass == stationClass.A)
						{
							msgNumber++;
							long diff = p.timeStamp - p.arrivalTime;
							if (diff > 1000)
							{
								System.err
										.println("[DEBUG] TIME NOT SYNC");
							}//else
//							{
							diffsum += diff;
//							}
						}
					}

					long incOffset;
					if (msgNumber != 0)
					{
						incOffset = diffsum / msgNumber;
					} else
					{
						incOffset = 0;
					}
					syncOffset += incOffset;
					//System.out.println("New syncoffset set to + " + _syncOffset);

					previousFrame = currentFrame;
					accessMessageList(Definition.AccessMode.CLEAR, null);

					synchronized (occupiedSlots)
					{
						occupiedSlots.clear();
					}
					try {
						Thread.sleep(999);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
