import java.util.ArrayList;
import java.util.List;

public class PacketList {
	
	public List<Packet> packetList;

	
	public PacketList copy() {
		List<Packet> copyList = new ArrayList<Packet>();

		for (Packet p : packetList)
		{
			copyList.add(p.copy());
		}

		PacketList copy = new PacketList();
		copy.packetList = copyList;

		return copy;
	}


}
