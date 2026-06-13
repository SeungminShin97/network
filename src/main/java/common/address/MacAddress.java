package common.address;

import java.util.Objects;

/**
 * 가상 네트워크에서 사용하는 MAC 주소 값 객체
 */
public record MacAddress(
    String value
) {
    public MacAddress {
        Objects.requireNonNull(value, "mac address cannot be null"); }

    /**
     * Ethernet Broadcast 주소
     */
    public static final MacAddress BROADCAST = new MacAddress("FF:FF:FF:FF:FF:FF");

    public boolean isBroadcast() { return this.equals(BROADCAST); }
} 
