package protocol.arp;

import common.address.Ipv4Address;
import common.address.MacAddress;
import protocol.ethernet.EtherType;
import protocol.ethernet.EthernetPayload;

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
}
