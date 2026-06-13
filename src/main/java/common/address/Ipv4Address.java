package common.address;

import java.util.Objects;

/**
 * 가상 네트워크에서 사용하는 IPv4 주소 값 객체.
 */
public record Ipv4Address(
        String value
) {
    public Ipv4Address {
        Objects.requireNonNull(value, "ip address cannot be null");
    }
}
