package protocol.arp;

/**
 * ARP OPER(ar$op) 값.
 * <p>
 * ARP 패킷이 요청인지 응답인지 구분<br>
 * 패킷에서는 16-bit unsigned 값으로 인코딩
 * </p>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc826">RFC 826</a>
 */
public enum ArpOperation {
    /**
     * ARP Request. 대상 IP의 MAC 주소를 요청한다.
     */
    REQUEST(1),

    /**
     * ARP Reply. 요청받은 IP에 대한 MAC 주소를 응답한다.
     */
    REPLY(2);

    private final int code;

    ArpOperation(int code) { this.code = code; }

    public int code() { return code; }

    public static ArpOperation fromCode(int code) {
        return switch (code) {
            case 1 -> REQUEST;
            case 2 -> REPLY;
            default -> throw new IllegalArgumentException("unsupported arp operation: " + code);
        };
    }
}
