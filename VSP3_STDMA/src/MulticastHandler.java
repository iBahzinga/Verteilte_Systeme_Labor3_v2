import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

public class MulticastHandler {

	private InetAddress groupAdress;
	//private MulticastSocket senderSocket;
	private java.net.MulticastSocket socket;
	private int port;
	private String ipAddress;
	private String ethernetName;

	public MulticastHandler(int port, String ipAdress, String ethernetName) {
		this.port = port;
		this.ipAddress = ipAdress;
		this.ethernetName = ethernetName;
	}

	public boolean init() {
		try {
			//senderSocket = new MulticastSocket(this.port);
			socket = new java.net.MulticastSocket(this.port);

			if (!ethernetName.equals("none")) {
				//senderSocket.setNetworkInterface(NetworkInterface.getByName(ethernetName));
				socket.setNetworkInterface(NetworkInterface.getByName(ethernetName));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroy();
			return false;
		}
		try {
			groupAdress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroy();
			return false;
		}

		try {
			//senderSocket.joinGroup(groupAdress); // Unnotig ??
			socket.joinGroup(groupAdress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroy();
			return false;
		}
		return true;
	}

	public boolean send(byte[] dataBuffer) {

		if (dataBuffer.length != Definition.FRAME_SIZE) {
			return false;
		}

		DatagramPacket datagramPacket = new DatagramPacket(dataBuffer, Definition.FRAME_SIZE, groupAdress, port);

		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroy();
			return false;
		}

		return true;
	}

	public boolean receive(DatagramPacket datagramPacket) {

		if (datagramPacket.getData().length != Definition.FRAME_SIZE) {
			return false;
		}

		try {
			socket.receive(datagramPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			destroy();
			return false;
		}

		return true;
	}

	private void destroy() {
		//senderSocket.close();
		socket.close();
		groupAdress = null;
	}

}
