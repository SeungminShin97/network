package protocol.arp;

import common.address.Ipv4Address;
import common.address.MacAddress;
import protocol.ethernet.EtherType;
import protocol.ethernet.EthernetPayload;

import java.util.Objects;

/**
 * ARP Packet.
 * <p>
 * IP 주소에 대응하는 MAC 주소를 찾기 위한 패킷<br>
 * SHA/SPA/THA/TPA는 각각 송신자 MAC, 송신자 IP, 대상 MAC, 대상 IP를 의미
 * </p>
 *
 * @param hardwareType HTYPE. 하드웨어 주소 타입
 * @param protocolType PTYPE. 프로토콜 주소 타입
 * @param hardwareLength HLEN. 하드웨어 주소 길이
 * @param protocolLength PLEN. 프로토콜 주소 길이
 * @param operation OPER. ARP 요청/응답 구분
 * @param senderHardwareAddress SHA. 송신자 MAC 주소
 * @param senderProtocolAddress SPA. 송신자 IPv4 주소
 * @param targetHardwareAddress THA. 대상 MAC 주소
 * @param targetProtocolAddress TPA. 대상 IPv4 주소
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc826">RFC 826</a>
 */
public record ArpPacket(
        HardwareType hardwareType,
        EtherType protocolType,
        int hardwareLength,
        int protocolLength,
        ArpOperation operation,
        MacAddress senderHardwareAddress,
        Ipv4Address senderProtocolAddress,
        MacAddress targetHardwareAddress,
        Ipv4Address targetProtocolAddress
) implements EthernetPayload {
    private static final int ETHERNET_ADDRESS_LENGTH = 6;
    private static final int IPV4_ADDRESS_LENGTH = 4;

    public ArpPacket {
        Objects.requireNonNull(hardwareType, "hardware type cannot be null");
        Objects.requireNonNull(protocolType, "protocol type cannot be null");
        Objects.requireNonNull(operation, "arp operation cannot be null");
        Objects.requireNonNull(senderHardwareAddress, "sender hardware address cannot be null");
        Objects.requireNonNull(senderProtocolAddress, "sender protocol address cannot be null");
        Objects.requireNonNull(targetHardwareAddress, "target hardware address cannot be null");
        Objects.requireNonNull(targetProtocolAddress, "target protocol address cannot be null");

        if (hardwareLength <= 0) {
            throw new IllegalArgumentException("hardware length must be positive");
        }

        if (protocolLength <= 0) {
            throw new IllegalArgumentException("protocol length must be positive");
        }
    }

    public static ArpPacket request(
            MacAddress senderHardwareAddress,
            Ipv4Address senderProtocolAddress,
            Ipv4Address targetProtocolAddress
    ) {
        return new ArpPacket(
                HardwareType.ETHERNET,
                EtherType.IPV4,
                ETHERNET_ADDRESS_LENGTH,
                IPV4_ADDRESS_LENGTH,
                ArpOperation.REQUEST,
                senderHardwareAddress,
                senderProtocolAddress,
                MacAddress.ZERO,
                targetProtocolAddress
        );
    }

    public static ArpPacket reply(
            MacAddress senderHardwareAddress,
            Ipv4Address senderProtocolAddress,
            MacAddress targetHardwareAddress,
            Ipv4Address targetProtocolAddress
    ) {
        return new ArpPacket(
                HardwareType.ETHERNET,
                EtherType.IPV4,
                ETHERNET_ADDRESS_LENGTH,
                IPV4_ADDRESS_LENGTH,
                ArpOperation.REPLY,
                senderHardwareAddress,
                senderProtocolAddress,
                targetHardwareAddress,
                targetProtocolAddress
        );
    }
}
