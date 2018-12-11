package me.qtill.commons.net;

import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class IPUtilTest {


    private InetAddress address = Inet4Address.getLocalHost();

    public IPUtilTest() throws UnknownHostException {
    }

    @Test
    public void toInt() {
        System.out.println(IPUtil.toIpString(address));
    }

    @Test
    public void toIpString() {
    }

    @Test
    public void fromInt() {
    }

    @Test
    public void fromIpString() {
    }

    @Test
    public void fromIpv4String() {
    }

    @Test
    public void intToIpv4String() {
    }

    @Test
    public void ipv4StringToInt() {
    }
}