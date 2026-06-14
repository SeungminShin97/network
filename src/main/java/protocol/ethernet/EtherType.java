package protocol.ethernet;

/**
 * Ethernet II Type 값.
 *
 * <p>payload 프로토콜을 구분
 * 패킷에서는 16-bit unsigned 값으로 인코딩
 *
 * @see <a href="https://www.iana.org/assignments/ieee-802-numbers/ieee-802-numbers.xhtml">IANA IEEE 802 Numbers</a>
 */
public enum EtherType {

    /**
     * IPv4 payload.
     */
    IPV4(0x0800),

    /**
     * ARP payload.
     */
    ARP(0x0806);

    private final int code;

    EtherType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static EtherType fromCode(int code) {
        return switch (code) {
            case 0x0800 -> IPV4;
            case 0x0806 -> ARP;
            default -> throw new IllegalArgumentException("unknown ether type: " + code);
        };
    }
}
