package node.host;

import common.address.Ipv4Address;
import common.address.MacAddress;

import java.util.Objects;

/**
 * Host 설정 값 객체
 * @param hostName Host가 사용하는 이름, 공백 불가
 * @param macAddress MAC 주소
 * @param ipv4Address IPv4 주소
 */
public record HostConfig(
        String hostName,
        MacAddress macAddress,
        Ipv4Address ipv4Address
) {
    public HostConfig {
        Objects.requireNonNull(hostName, "host name cannot be null");
        Objects.requireNonNull(macAddress, "mac address cannot be null");
        Objects.requireNonNull(ipv4Address, "ipv4 address cannot be null");

        if (hostName.isBlank()) throw new IllegalArgumentException("host name cannot be blank");
    }
}
