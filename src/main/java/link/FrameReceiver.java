package link;

import protocol.ethernet.EthernetFrame;

/**
 * EthernetFrame 수신 endpoint.
 * <p>
 * VirtualLink로부터 EthernetFrame을 받을 수 있는 객체가 구현<br>
 * 송신 방식은 정의하지 않고, 수신 진입점만 제공
 * </p>
 */
public interface FrameReceiver {
    void receive(EthernetFrame frame);
}
