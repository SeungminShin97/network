package node.host;

import common.address.Ipv4Address;
import common.address.MacAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * ARP Cache <br>
 * IPv4 주소와 MAC 주소의 매핑을 저장
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc826">RFC 826 - Address Resolution Protocol</a>
 */
public class ArpCache {

    private final Map<Ipv4Address, MacAddress> map = new HashMap<>();

    /**
     * IPv4 주소와 MAC 주소 매핑을 저장 <br>
     * 기존에 주소가 있으면 덮어씀
     *
     * @param ip IPv4 주소
     * @param mac MAC 주소
     */
    public void put(Ipv4Address ip, MacAddress mac) {
        Objects.requireNonNull(ip, "ipv4 address cannot be null");
        Objects.requireNonNull(mac, "mac address cannot be null");
        map.put(ip, mac);
    }

    /**
     * IPv4 주소에 대응되는 MAC 주소를 조회 <br>
     * 매핑이 없으면 {@link Optional#empty()}를 반환한다.
     *
     * @param ip 조회할 IPv4 주소
     * @return IPv4 주소에 대응되는 MAC 주소
     */
    public Optional<MacAddress> find(Ipv4Address ip) {
        Objects.requireNonNull(ip, "ipv4 address cannot be null");
        return Optional.ofNullable(map.get(ip));
    }

    /**
     * IPv4 주소로 MAC 주소 검색
     *
     * @param ip 조회할 IPv4 주소
     * @return 매핑이 존재하면 true, 아니면 false
     */
    public boolean contains(Ipv4Address ip) {
        Objects.requireNonNull(ip, "ipv4 address cannot be null");
        return map.containsKey(ip);
    }
}
