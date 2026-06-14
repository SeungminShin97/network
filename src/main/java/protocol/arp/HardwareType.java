package protocol.arp;

/**
 * ARP HTYPE(ar$hrd) 값.
 *
 * <p>패킷에서는 16-bit unsigned 값으로 인코딩
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc826">RFC 826</a>
 */
public enum HardwareType {
    ETHERNET(1);

    private final int code;

    HardwareType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static HardwareType fromCode(int code) {
        return switch (code) {
            case 1 -> ETHERNET;
            default -> throw new IllegalArgumentException(
                    "unsupported hardware type: " + code
            );
        };
    }
}
