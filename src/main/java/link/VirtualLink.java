package link;

import protocol.ethernet.EthernetFrame;

/**
 * 단일 JVM 시뮬레이션용 가상 링크.
 * <p>
 * 두 endpoint를 연결하고, 한쪽 endpoint에서 들어온 EthernetFrame을 반대쪽 endpoint로 전달<br>
 * MAC 주소 판단, Broadcast 판단, Switching, Routing은 수행하지 않는다.
 * </p>
 */
public class VirtualLink {

    private final FrameReceiver endPointA;
    private final FrameReceiver endPointB;

    public VirtualLink(FrameReceiver endPointA, FrameReceiver endPointB) {
        this.endPointA = endPointA;
        this.endPointB = endPointB;
    }

    /**
     * EthernetFrame을 반대편 endpoint로 전달<br>
     * 이 링크에 연결되지 않은 source가 들어오면 예외를 발생시킨다.
     *
     * @param source Frame을 보낸 endpoint
     * @param frame 전달할 EthernetFrame
     */
    public void transmit(FrameReceiver source, EthernetFrame frame) {
        if(source == endPointA) {
            endPointB.receive(frame);
            return;
        }

        if(source == endPointB) {
            endPointA.receive(frame);
            return;
        }

        throw new IllegalArgumentException("source is not connected to this link");
    }
}
