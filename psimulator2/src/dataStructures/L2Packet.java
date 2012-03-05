/*
 * Erstellt am 26.10.2011.
 */
package dataStructures;

/**
 *
 * @author neiss
 */
public abstract class L2Packet {

    public final L3Packet data;

	// asi nebude potreba, nevim
	public L2Packet() {
		this.data = null;
	}

	public L2Packet(L3Packet data) {
		this.data = data;
	}

	public enum L2PacketType{
		ethernetII;
	}

    // TODO: getSize() cachovat, jinak bude pekne narocnej
    public abstract int getSize();

	public abstract L2PacketType getType();
}