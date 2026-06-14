package protocol.ethernet;

import common.address.MacAddress;

/**
 * Ethernet II Frame.
 * <p>
 * MAC 주소와 payload 프로토콜 타입을 가지는 L2 Frame<br>
 * payload는 EtherType 값에 따라 ARP, IPv4 같은 상위 프로토콜로 해석
 * </p>
 *
 * @param dstMac 목적지 MAC 주소
 * @param srcMac 출발지 MAC 주소
 * @param etherType payload 프로토콜 타입
 * @param payload Ethernet payload
 * @see <a href="https://standards.ieee.org/ieee/802.3/10422/">IEEE 802.3</a>
 * @see <a href="https://www.iana.org/assignments/ieee-802-numbers/ieee-802-numbers.xhtml">IANA IEEE 802 Numbers</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc894">RFC 894</a>
 */
public record EthernetFrame(
        MacAddress dstMac,
        MacAddress srcMac,
        EtherType etherType,
        EthernetPayload payload
) {
}
