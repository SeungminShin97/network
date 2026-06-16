package node.host;

import common.address.Ipv4Address;
import common.address.MacAddress;
import protocol.arp.ArpOperation;
import protocol.arp.ArpPacket;
import protocol.ethernet.EtherType;
import protocol.ethernet.EthernetFrame;

import java.util.Objects;
import java.util.Optional;

/**
 * Host의 ARP 처리기 <br>
 * ARP Request Frame 생성, ARP Request 수신 처리, ARP Reply 수신 처리를 담당
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc826">RFC 826 - Address Resolution Protocol</a>
 */
public class ArpHandler {

    private final HostConfig config;
    private final ArpCache cache;

    public ArpHandler(HostConfig config, ArpCache cache) {
        this.config = Objects.requireNonNull(config, "host config cannot be null");
        this.cache = Objects.requireNonNull(cache, "arp cache cannot be null");
    }

    /**
     * ARP Request EthernetFrame을 생성
     * <p>
     * EthernetFrame의 목적지 MAC은 Broadcast MAC으로 설정<br>
     * ARP Packet의 target hardware address는 아직 모르므로 zero MAC으로 설정
     * </p>
     *
     * @param targetIp MAC 주소를 알고 싶은 대상 IPv4 주소
     * @return ARP Request를 담은 EthernetFrame
     */
    public EthernetFrame createRequest(Ipv4Address targetIp) {
        Objects.requireNonNull(targetIp, "target ip cannot be null");

        ArpPacket packet = ArpPacket.request(
                config.macAddress(),
                config.ipv4Address(),
                targetIp
        );

        return new EthernetFrame(
                MacAddress.BROADCAST,
                config.macAddress(),
                EtherType.ARP,
                packet
        );
    }

    /**
     * ARP 처리 <br>
     * ARP Frame이 아니면 처리 안함 <br>
     * ARP Request의 dst Ip가 내 IP라면 ARP Reply 반환
     *
     * @param frame 수신한 EthernetFrame
     * @return 응답으로 송신해야 하는 EthernetFrame
     */
    public Optional<EthernetFrame> handle(EthernetFrame frame) {
        Objects.requireNonNull(frame, "ethernet frame cannot be null");

        if(frame.etherType() != EtherType.ARP) return Optional.empty();

        if(!(frame.payload() instanceof ArpPacket packet))
            throw new IllegalArgumentException("ARP frame payload must be ArpPacket");

        return handle(packet);
    }

    /**
     * ARP operation에 따른 처리
     *
     * @param packet 수신한 ARP Packet
     * @return 응답으로 송신해야 하는 EthernetFrame
     */
    private Optional<EthernetFrame> handle(ArpPacket packet) {
        if(packet.operation() == ArpOperation.REQUEST) return handleRequest(packet);

        if(packet.operation() == ArpOperation.REPLY) {
            handleReply(packet);
            return Optional.empty();
        }

        throw new IllegalArgumentException("unsupported ARP operation: " + packet.operation());
    }

    /**
     * ARP Request를 처리
     * <p>
     * Request를 보낸 송신자의 IP/MAC 을 ARP Cache에 저장 <br>
     * 송신자의 IP 주소가 내 IPv4 주소와 같으면 ARP Reply Frame을 생성
     * </p>
     *
     * @param request 수신한 ARP Request
     * @return 응답으로 송신해야 하는 ARP Reply Frame
     */
    private Optional<EthernetFrame> handleRequest(ArpPacket request) {
        cache.put(request.senderProtocolAddress(), request.senderHardwareAddress());

        if(!request.targetProtocolAddress().equals(config.ipv4Address()))
            return Optional.empty();

        ArpPacket reply = ArpPacket.reply(
                config.macAddress(),
                config.ipv4Address(),
                request.senderHardwareAddress(),
                request.senderProtocolAddress()
        );

        EthernetFrame frame = new EthernetFrame(
                request.senderHardwareAddress(),
                config.macAddress(),
                EtherType.ARP,
                reply
        );

        return Optional.of(frame);
    }

    /**
     * Reply를 보낸 송신자의 IP/MAC을 ARP Cache에 저장
     *
     * @param reply 수신한 ARP Reply
     */
    private void handleReply(ArpPacket reply) {
        cache.put(reply.senderProtocolAddress(), reply.senderHardwareAddress());
    }

    /**
     * @param ip 찾고싶은 MAC 주소의 IP 주소
     * @return MAC 주소
     */
    public Optional<MacAddress> findCachedMac(Ipv4Address ip) {
        return cache.find(ip);
    }
}
