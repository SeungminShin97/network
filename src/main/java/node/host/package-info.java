/**
 * Host 노드와 Host 내부 ARP 처리 구성 요소를 제공하는 패키지.
 * <p>
 * 이 패키지는 단일 JVM 시뮬레이션에서 Host의 설정, ARP Cache, ARP Request/Reply 처리,
 * 그리고 Virtual NIC로부터 올라온 EthernetFrame 수신 진입점을 담당한다.<br>
 * Frame 전달은 link 패키지의 VirtualNic/VirtualLink에 위임하며,
 * Switching, Routing, 실제 Frame 송신 방식은 담당하지 않는다.
 * </p>
 *
 * @see node.host.Host
 * @see node.host.HostConfig
 * @see node.host.ArpHandler
 * @see node.host.ArpCache
 */
package node.host;