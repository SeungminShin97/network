package link;

import common.address.MacAddress;
import protocol.ethernet.EthernetFrame;

/**
 * Host가 가진 가상 NIC.
 * <p>
 * 자기 MAC 주소를 가지고, 송신 Frame은 연결된 VirtualLink로 내보낸다.<br>
 * 수신 Frame은 목적지 MAC이 자기 MAC이거나 Broadcast인 경우에만 owner에게 전달한다.
 * </p>
 */
public class VirtualNic implements FrameReceiver{

    private final MacAddress macAddress;
    private final FrameReceiver owner;

    private VirtualLink link;

    /**
     * VirtualNic을 생성한다.
     * <p>
     * macAddress는 이 NIC 자신의 MAC 주소다.<br>
     * owner는 이 NIC가 수신한 Frame을 상위로 전달할 대상이다.
     * </p>
     *
     * @param macAddress 이 NIC의 MAC 주소
     * @param owner 수신 Frame을 전달받을 상위 객체
     */
    public VirtualNic(MacAddress macAddress, FrameReceiver owner) {
        this.macAddress = macAddress;
        this.owner = owner;
    }

    /**
     * 이 NIC의 MAC 주소를 반환한다.
     *
     * @return 이 NIC의 MAC 주소
     */
    public MacAddress macAddress() {
        return macAddress;
    }

    /**
     * 이 NIC를 VirtualLink에 연결한다.
     *
     * @param link 연결할 VirtualLink
     */
    public void connect(VirtualLink link) {
        this.link = link;
    }

    /**
     * EthernetFrame을 송신한다.
     * <p>
     * 연결된 VirtualLink로 Frame을 전달한다.<br>
     * 아직 링크가 연결되지 않았다면 IllegalStateException을 발생시킨다.
     * </p>
     *
     * @param frame 송신할 EthernetFrame
     */
    public void send(EthernetFrame frame) {
        if(link == null) throw new IllegalStateException("virtualNic is not connected");

        link.transmit(this, frame);
    }

    /**
     * EthernetFrame을 수신한다.
     * <p>
     * 목적지 MAC이 자기 MAC이거나 Broadcast MAC인 경우에만 owner에게 전달한다.<br>
     * 그 외 Frame은 이 NIC의 대상이 아니므로 drop한다.
     * </p>
     *
     * @param frame 수신한 EthernetFrame
     */
    @Override
    public void receive(EthernetFrame frame) {
        if(!frame.dstMac().equals(macAddress) && !frame.dstMac().isBroadcast()) return;

        owner.receive(frame);
    }
}
