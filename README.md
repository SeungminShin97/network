# Java Virtual Network Lab

## 1. 프로젝트 개요

Java 기반 가상 네트워크 스택 실험 환경이다.

이 프로젝트는 Host, Switch, Gateway/Router를 Java 객체로 모델링하고, ARP, Ethernet, IPv4, Routing, TCP-like 상태 전이, HTTP Payload 전달 흐름을 직접 구현한다.

초기 버전은 Java 객체와 VirtualLink 기반의 시뮬레이션 모드로 구현한다. 이후 Transport 계층을 분리하여 pcap4j, Raw Socket, TAP 인터페이스 기반의 실제 네트워크 인터페이스 송수신 실험으로 확장할 수 있도록 설계한다.

---

## 2. 프로젝트 목적

목적은 실제 네트워크 장비를 대체하는 것이 아니라, 네트워크 계층 동작을 코드로 재현하고 관찰하는 것이다.

주요 학습 대상은 다음과 같다.

* ARP Request / Reply
* MAC Address 기반 L2 Switching
* Switch MAC Learning
* Broadcast Flooding
* Unknown Unicast Flooding
* IPv4 Packet 전달
* Gateway / Router의 Routing
* TTL 감소
* Ethernet Header 재작성
* TCP 3-way handshake 흐름
* HTTP Payload 전달 흐름

---

## 3. 프로젝트 범위

### 직접 구현하는 것

* Virtual NIC
* Virtual Link
* Ethernet Frame
* ARP Packet
* ARP Cache
* IPv4 Packet
* Routing Table
* Gateway / Router
* L2 Switch
* TCP-like State Machine
* HTTP Payload 전달
* Event Timeline / Trace Log

### 초기 버전에서 구현하지 않는 것

* Linux Kernel TCP/IP Stack 대체
* Docker Bridge 대체
* 물리 NIC 제어
* 실제 Ethernet Frame 송신
* 완전한 TCP RFC 구현
* TCP 재전송
* 혼잡 제어
* Flow Control
* 동적 라우팅 프로토콜

단, 실제 Ethernet Frame 송수신은 이후 확장 모드에서 pcap4j, Raw Socket, TAP 인터페이스로 실험할 수 있도록 Transport 계층을 분리한다.

---

## 4. 핵심 포지셔닝

이 프로젝트는 다음과 같이 정의한다.

> Java 애플리케이션 레벨에서 ARP, Ethernet, IPv4, Routing, TCP-like 상태 전이를 직접 구현한 가상 TCP/IP Stack 실험 환경

주의할 점은 다음과 같다.

* OS 커널 TCP/IP Stack을 대체하지 않는다.
* 실제 Router 장비를 구현하는 것이 아니다.
* 실제 NIC를 통해 Ethernet Frame을 송신하는 것은 초기 범위가 아니다.
* 초기 구현은 Java 객체와 byte[] 메시지 기반이다.
* 이후 Transport 구현체를 교체하여 실제 NIC 송수신 실험으로 확장 가능하게 설계한다.

---

## 5. 전체 네트워크 구조

최소 3개 네트워크와 하나의 Backbone Network를 둔다.

```text
[Network n1: 10.0.1.0/24]
  n1h1
  n1h2
  n1sw
  n1gw

[Network n2: 10.0.2.0/24]
  n2h1
  n2h2
  n2sw
  n2gw

[Network n3: 10.0.3.0/24]
  n3h1
  n3h2
  n3sw
  n3gw

[Backbone Network: 10.0.255.0/24]
  n1gw
  n2gw
  n3gw
```

전체 흐름은 다음과 같다.

```text
n1h1 ─┐
n1h2 ─┼── n1sw ── n1gw ─┐
      ┘                 │
                        │
n2h1 ─┐                 │
n2h2 ─┼── n2sw ── n2gw ─┼── backbone
      ┘                 │
                        │
n3h1 ─┐                 │
n3h2 ─┼── n3sw ── n3gw ─┘
      ┘
```

---

## 6. 구성 요소

## 6.1 Host

Host는 일반 PC 또는 서버 역할을 한다.

Host가 가지는 정보는 다음과 같다.

```text
- Host name
- Virtual IP
- Virtual MAC
- Subnet mask
- Default gateway IP
- ARP cache
- TCP connection table
- Virtual NIC
```

Host의 책임은 다음과 같다.

```text
1. 목적지 IP가 같은 네트워크인지 판단한다.
2. 같은 네트워크면 목적지 IP의 MAC을 ARP로 찾는다.
3. 다른 네트워크면 Default Gateway의 MAC을 ARP로 찾는다.
4. Ethernet Frame을 생성한다.
5. IPv4 Packet을 생성한다.
6. TCP Segment를 생성한다.
7. HTTP Payload를 생성한다.
8. 수신한 Frame을 계층별로 역파싱한다.
```

---

## 6.2 Virtual NIC

Virtual NIC는 실제 NIC를 대체하는 Java 객체다.

Virtual NIC는 자기 자신의 MAC 주소와 IP 주소를 가진다.

예시:

```text
n1h1 vNIC
  IP  = 10.0.1.10
  MAC = AA:AA:AA:AA:AA:01
```

역할은 다음과 같다.

```text
1. 상위 계층에서 생성한 Ethernet Frame을 Link로 내보낸다.
2. Link로부터 수신한 Ethernet Frame을 Host로 전달한다.
3. 자신의 MAC 주소를 기준으로 Frame 수신 여부를 판단한다.
```

---

## 6.3 Virtual Link

Virtual Link는 실제 랜선을 모델링한 객체다.

Virtual Link는 판단하지 않는다.

하지 않는 일:

```text
- MAC 주소 판단
- IP 주소 판단
- Routing
- Switching
- Broadcast 결정
```

하는 일:

```text
- 한쪽 끝에서 받은 Frame을 반대쪽 끝으로 전달한다.
```

예시:

```text
n1h1 vNIC --- VirtualLink --- n1sw port1
n1h2 vNIC --- VirtualLink --- n1sw port2
n1gw vNIC --- VirtualLink --- n1sw port3
```

중요한 점:

```text
link1은 h1과 switch port1만 안다.
link1은 h2, h3, gateway를 모른다.
broadcast는 link가 아니라 switch가 수행한다.
```

---

## 6.4 Switch

Switch는 L2 장비다.

Switch가 가지는 정보는 다음과 같다.

```text
- Switch name
- Port 목록
- MAC Address Table
```

Switch의 책임은 다음과 같다.

```text
1. Frame을 수신한다.
2. Source MAC을 보고 MAC Table에 학습한다.
3. Destination MAC을 확인한다.
4. Broadcast Frame이면 수신 포트를 제외한 모든 포트로 Flooding한다.
5. MAC Table에 목적지가 있으면 해당 포트로 Forwarding한다.
6. 목적지를 모르면 Unknown Unicast Flooding한다.
```

Switch는 IP를 보지 않는다.

```text
Switch 판단 기준 = MAC 주소
Router 판단 기준 = IP 주소
```

---

## 6.5 Gateway / Router

Gateway는 네트워크 바깥으로 나가는 통로이며 Router 역할을 수행한다.

Gateway는 여러 Interface를 가진다.

예시:

```text
n1gw

LAN Interface
  IP  = 10.0.1.1
  MAC = GW1-LAN-MAC

Backbone Interface
  IP  = 10.0.255.1
  MAC = GW1-BB-MAC
```

Gateway의 책임은 다음과 같다.

```text
1. 자기 IP에 대한 ARP Request에 응답한다.
2. 외부 네트워크 목적지의 IPv4 Packet을 수신한다.
3. IPv4 Destination IP를 확인한다.
4. Routing Table을 조회한다.
5. TTL을 감소시킨다.
6. Next Hop을 결정한다.
7. Next Hop의 MAC 주소를 ARP로 확인한다.
8. Ethernet Header를 새로 만들어 다음 네트워크로 전달한다.
```

중요한 규칙:

```text
IP 목적지는 최종 목적지로 유지된다.
MAC 목적지는 매 Hop마다 바뀐다.
```

---

## 7. 통신 시나리오

## 7.1 같은 네트워크 통신

예시:

```text
n1h1 → n1h2
```

흐름:

```text
1. n1h1이 목적지 IP 10.0.1.20을 확인한다.
2. 자기 subnet 10.0.1.0/24 안이므로 같은 네트워크라고 판단한다.
3. ARP Cache에서 10.0.1.20을 조회한다.
4. 없으면 ARP Request를 생성한다.
5. ARP Request는 Broadcast MAC으로 전송된다.
6. n1h1 vNIC → VirtualLink → n1sw port1로 전달된다.
7. n1sw는 Source MAC을 학습한다.
8. n1sw는 Broadcast Frame을 Flooding한다.
9. n1h2가 ARP Request를 수신하고 ARP Reply를 보낸다.
10. n1h1은 ARP Cache에 10.0.1.20 → n1h2 MAC을 저장한다.
11. n1h1은 실제 IPv4/TCP Payload Frame을 생성한다.
12. n1sw는 MAC Table을 보고 n1h2로 Forwarding한다.
```

핵심:

```text
Gateway를 거치지 않는다.
Switch만 거친다.
```

---

## 7.2 다른 네트워크 통신

예시:

```text
n1h1 → n2h1
```

흐름:

```text
1. n1h1이 목적지 IP 10.0.2.10을 확인한다.
2. 자기 subnet 10.0.1.0/24 밖이라고 판단한다.
3. Default Gateway 10.0.1.1로 보내기로 결정한다.
4. ARP Cache에서 10.0.1.1을 조회한다.
5. 없으면 n1gw에 대해 ARP Request를 보낸다.
6. n1gw가 ARP Reply를 보낸다.
7. n1h1은 Ethernet dstMac을 n1gw MAC으로 설정한다.
8. IPv4 dstIp는 10.0.2.10으로 유지한다.
9. n1h1 → n1sw → n1gw로 전달된다.
10. n1gw가 Routing Table을 조회한다.
11. 10.0.2.0/24는 n2gw로 보내야 한다고 판단한다.
12. backbone에서 n2gw MAC을 ARP로 확인한다.
13. n1gw는 Ethernet Header를 새로 만든다.
14. n1gw → backbone → n2gw로 전달된다.
15. n2gw가 10.0.2.10이 자기 local network라고 판단한다.
16. n2h1 MAC을 ARP로 확인한다.
17. n2gw → n2sw → n2h1로 전달된다.
```

핵심:

```text
Host는 최종 목적지 IP를 유지한다.
Gateway는 다음 Hop으로 넘긴다.
Switch는 MAC 기반 전달만 한다.
```

---

## 8. 구현할 핵심 프로토콜

## 8.1 Ethernet

구현 필드:

```text
- dstMac
- srcMac
- etherType
- payload
```

처리:

```text
etherType = ARP  → ARP Parser
etherType = IPv4 → IPv4 Parser
```

---

## 8.2 ARP

구현 기능:

```text
- ARP Request
- ARP Reply
- ARP Cache
- Retry
- Timeout
```

목표:

```text
IP 주소를 MAC 주소로 변환한다.
```

---

## 8.3 IPv4

구현 필드:

```text
- srcIp
- dstIp
- ttl
- protocol
- payload
```

처리:

```text
ttl <= 0 → drop
protocol = TCP → TCP Parser
```

IPv4 Fragmentation은 MVP에서 제외한다.

---

## 8.4 Routing

구현 기능:

```text
- Routing Table
- Local Network 판단
- Next Hop 결정
- Out Interface 결정
- TTL 감소
- ARP로 Next Hop MAC 조회
- Ethernet Header 재작성
```

초기에는 Static Routing 기반으로 구현한다.

---

## 8.5 TCP-like State Machine

초기 목표:

```text
- 3-way handshake 흐름 재현
- Payload 전달
- FIN 종료
```

처리 플래그:

```text
SYN
ACK
PSH
FIN
RST
```

초기 상태:

```text
CLOSED
LISTEN
SYN_SENT
SYN_RECEIVED
ESTABLISHED
FIN_WAIT
CLOSE_WAIT
LAST_ACK
```

제외 범위:

```text
- 재전송
- 혼잡 제어
- Flow Control
- 완전한 RFC 수준 TCP 구현
```

---

## 8.6 HTTP Payload

초기에는 문자열 Payload로 시작한다.

```text
GET /health HTTP/1.1
Host: n2h1
```

최종적으로 SeungPring HTTP Parser와 연동한다.

---

## 9. Transport 계층

이 프로젝트는 상위 네트워크 로직과 실제 전달 방식을 분리한다.

상위 로직:

```text
Host
Switch
Gateway
ARP
Ethernet
IPv4
Routing
TCP-like State
```

전달 방식:

```text
VirtualLink
InMemoryTransport
DockerTransport
PcapTransport
RawSocketTransport
TapTransport
```

Transport 인터페이스 예시:

```java
public interface FrameTransport {
    void send(FrameEndpoint endpoint, byte[] ethernetFrameBytes);
}
```

수신 인터페이스 예시:

```java
public interface FrameReceiver {
    void onReceive(byte[] ethernetFrameBytes);
}
```

이 구조를 통해 초기에는 InMemory 기반으로 개발하고, 이후 실제 인터페이스 기반 송수신으로 확장할 수 있다.

---

## 10. 실행 모드

## 10.1 Simulation Mode

모든 노드를 하나의 JVM 객체로 실행한다.

```text
Java Process
 ├─ n1h1
 ├─ n1h2
 ├─ n1sw
 ├─ n1gw
 ├─ n2h1
 ├─ n2h2
 ├─ n2sw
 └─ n2gw
```

특징:

```text
- 디버깅이 쉽다.
- 테스트 작성이 쉽다.
- Docker나 OS 네트워크 설정에 덜 의존한다.
- VirtualLink가 객체 참조 기반으로 Frame을 전달한다.
```

---

## 10.2 Docker Transport Mode

각 노드를 컨테이너로 분리한다.

Docker는 실제 네트워크 구현 대상이 아니다.

Docker의 역할:

```text
- 컨테이너 분리 실행
- 실험 환경 격리
- Java Frame byte[] 전달 운반망
```

주의:

```text
Docker 내부 통신도 Linux Kernel TCP/IP Stack을 탄다.
일반 TCP/UDP Socket을 사용하면 Virtual EthernetFrame은 실제 TCP/UDP Payload 안에 들어간다.
```

구조:

```text
[Real Ethernet]
  [Real IP]
    [Real TCP/UDP]
      [Virtual EthernetFrame]
        [Virtual IPv4 Packet]
          [Virtual TCP Segment]
```

따라서 Docker Transport Mode는 실제 Ethernet Frame 송신이 아니라, 분산 실행을 위한 운반망이다.

---

## 10.3 Raw Interface Extension Mode

선택 확장 모드.

목표:

```text
Virtual EthernetFrame을 실제 네트워크 인터페이스로 송수신하는 실험
```

후보 기술:

```text
- pcap4j
- Raw Socket
- TAP Interface
```

이 모드에서는 다음 구조를 목표로 한다.

```text
Java Network Stack
 → EthernetFrame Encode
 → PcapTransport / RawSocketTransport / TapTransport
 → 실제 veth 또는 NIC
```

주의할 점:

```text
- 관리자 권한이 필요할 수 있다.
- OS별 차이가 크다.
- Docker Desktop / WSL2 환경에서는 복잡할 수 있다.
- OS Kernel TCP/IP Stack과 충돌할 수 있다.
- 직접 만든 TCP Segment는 OS TCP와 충돌할 수 있다.
- 격리된 실험 환경에서 진행해야 한다.
```

이 모드는 MVP 범위가 아니라 확장 기능이다.

---

## 11. Docker와 실제 OS 실행의 차이

Docker 컨테이너는 독립 OS가 아니다.

컨테이너는 Host OS Kernel을 공유하는 격리된 프로세스다.

Docker 내부에서 일반 Socket 통신을 하면 다음 흐름을 탄다.

```text
Java Process
 → Container Network Namespace
 → Linux Kernel TCP/IP Stack
 → veth
 → Docker Bridge
 → 상대 Container veth
 → 상대 Java Process
```

따라서 Docker 내부 통신도 Kernel TCP/IP Stack을 지난다.

이 프로젝트의 초기 Docker 모드는 실제 NIC 송신이 아니라, Java 가상 Frame을 다른 프로세스로 전달하는 분산 실행 모드다.

---

## 12. 확장 가능성을 위한 설계 원칙

1. 네트워크 계층 로직과 전달 방식을 분리한다.

```text
ARP / Ethernet / IPv4 / Routing / TCP-like
```

와

```text
InMemory / Docker / Pcap / Raw / TAP
```

를 분리한다.

2. Frame은 반드시 byte[]로 encode/decode 가능해야 한다.

```text
EthernetFrame 객체
→ byte[]
→ EthernetFrame 객체
```

3. Host, Switch, Gateway는 Transport 구현체를 직접 몰라야 한다.

```text
Host는 vNIC로 보낸다.
vNIC는 Link 또는 Transport를 통해 전달한다.
```

4. Docker endpoint는 Host 로직이 아니라 Transport/Topology 설정이 알아야 한다.

5. pcap4j 확장은 Transport 구현체 추가로 처리할 수 있게 한다.

---

## 13. 로그 추적 전략

모든 Frame/Packet/Segment에 추적용 메타데이터를 부여한다.

단, traceId는 실제 Ethernet Header 필드가 아니라 시뮬레이터 메타데이터다.

예시:

```json
{
  "traceId": "REQ-0001",
  "frameId": "ETH-0012",
  "packetId": "IP-0007",
  "segmentId": "TCP-0003",
  "hop": 4,
  "node": "n1gw",
  "event": "ROUTE_FORWARD",
  "nextHop": "10.0.255.2"
}
```

Simulation Mode에서는 EventCollector로 수집한다.

Docker Mode에서는 stdout JSON log 또는 중앙 Event Collector를 사용한다.

---

## 14. 개발 순서

## 14.1 1단계: 단일 네트워크

구성:

```text
n1h1
n1h2
n1sw
```

목표:

```text
n1h1 → n1h2 메시지 전달
ARP Request / Reply
Switch MAC Learning
Broadcast Flooding
Known Unicast Forwarding
```

---

## 14.2 2단계: Gateway 추가

구성:

```text
n1h1
n1h2
n1sw
n1gw
```

목표:

```text
외부 IP로 보낼 때 Default Gateway MAC을 ARP로 찾는지 확인한다.
```

---

## 14.3 3단계: 네트워크 2개

구성:

```text
n1
n2
backbone
```

목표:

```text
n1h1 → n2h1
```

경로:

```text
n1h1 → n1sw → n1gw → backbone → n2gw → n2sw → n2h1
```

---

## 14.4 4단계: 네트워크 3개

구성:

```text
n1
n2
n3
backbone
```

목표:

```text
n1h1 → n2h1
n1h1 → n3h1
n2h2 → n3h2
```

---

## 14.5 5단계: TCP-like 상태 전이

목표:

```text
SYN
SYN-ACK
ACK
PSH + HTTP Payload
FIN
```

---

## 14.6 6단계: Docker Transport Mode

목표:

```text
각 Host, Switch, Gateway를 컨테이너로 분리한다.
Docker Network는 Java Frame byte[] 전달 운반망으로만 사용한다.
```

---

## 14.7 7단계: Raw Interface Extension

선택 확장.

목표:

```text
pcap4j / Raw Socket / TAP 기반 실제 인터페이스 송수신 실험
```

---

## 15. 최종 정리

이 프로젝트의 핵심은 다음과 같다.

```text
Java 기반 가상 TCP/IP Stack 실험 환경
```

초기 구현:

```text
Java 객체 기반 Simulation Mode
```

분산 실행:

```text
Docker Transport Mode
```

확장 가능성:

```text
pcap4j / Raw Socket / TAP 기반 실제 인터페이스 송수신 실험
```

최종적으로 이 프로젝트는 실제 네트워크의 물리 송수신을 직접 대체하지 않지만, ARP, Ethernet, Switching, Routing, TCP 연결 흐름을 Java 코드로 직접 구현하고 관찰할 수 있는 실험 환경을 목표로 한다.
