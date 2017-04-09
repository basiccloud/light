package net.basiccloud.light.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * ip utils
 */
public class IpUtil {

    private static Logger logger = LoggerFactory.getLogger(IpUtil.class);

    public static List<String> getIpFromNetworkInterfaces() {
        List<String> ipList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.error("can't getNetworkInterfaces()", e);
            return ipList;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
            } catch (SocketException e) {
                logger.error("fail to check networkInterface status", e);
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (isValidIP(address)) {
                    ipList.add(address.getHostAddress());
                }
            }
        }

        return ipList;
    }

    public static Optional<String> getIpFromSocket() {
        Optional<String> result = getIpFromSocket("www.baidu.com", 80);
        if (result.isPresent()) {
            return result;
        }

        result = getIpFromSocket("www.163.com", 80);
        if (result.isPresent()) {
            return result;
        }

        result = getIpFromSocket("www.sina.com.cn", 80);
        return result;
    }

    public static Optional<String> getIpFromSocket(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            InetAddress localAddress = socket.getLocalAddress();
            if (!isValidIP(localAddress)) {
                return Optional.empty();
            }

            String ip= localAddress.getHostAddress();
            return Optional.of(ip);
        } catch (Exception e) {
            logger.error("fail to get IP because of network failure", e);
            return Optional.empty();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // no nothing
                }
            }
        }
    }

    private static boolean isValidIP(InetAddress address) {
        return !address.isLoopbackAddress() && address instanceof Inet4Address;
    }
}
