package node.host;

import common.address.Ipv4Address;
import link.FrameReceiver;
import link.VirtualNic;
import protocol.ethernet.EthernetFrame;

import java.util.Objects;

/**
 * 가상 네트워크의 Host 노드 <br>
 * Host는 Virtual NIC를 통해 EthernetFrame을 송수신
 */
public class Host implements FrameReceiver {

    private final HostConfig config;
    private final VirtualNic virtualNic;
    private final ArpHandler arpHandler;

    public Host(HostConfig config) {
        this.config = Objects.requireNonNull(config, "host config cannot be null");

        ArpCache arpCache = new ArpCache();
        this.arpHandler = new ArpHandler(config, arpCache);

        this.virtualNic = new VirtualNic(config.macAddress(), this);
    }

    /**
     * @return 호스트 이름
     */
    public String hostName() {
        return config.hostName();
    }

    /**
     * @return IPv4 주소
     */
    public Ipv4Address ipv4Address() {
        return config.ipv4Address();
    }

    /**
     * @return Host의 Virtual NIC
     */
    public VirtualNic nic() {
        return virtualNic;
    }

    /**
     * 대상 IPv4 주소에 대한 ARP Request를 송신
     * <p>
     * 이미 ARP Cache에 대상 IPv4 주소의 MAC 주소가 있으면 Request를 보내지 않음<br>
     * MAC 주소를 모르는 경우 ARP Request Frame을 생성해 Virtual NIC로 송신
     * </p>
     *
     * @param targetIp MAC 주소를 알고 싶은 대상 IPv4 주소
     */
    public void sendArpRequest(Ipv4Address targetIp) {
        Objects.requireNonNull(targetIp, "target ip cannot be null");

        if (arpHandler.findCachedMac(targetIp).isPresent()) return;

        EthernetFrame requestFrame = arpHandler.createRequest(targetIp);
        virtualNic.send(requestFrame);
    }

    /**
     * Virtual NIC로부터 전달받은 EthernetFrame을 처리 <br>
     * NIC에서 Host 네트워크 스택으로 올라오는 수신 진입점
     *
     * @param frame 수신한 EthernetFrame
     */
    @Override
    public void receive(EthernetFrame frame) {
        Objects.requireNonNull(frame, "ethernet frame cannot be null");

        arpHandler.handle(frame).ifPresent(virtualNic::send);
    }
}
