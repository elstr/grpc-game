package nl.toefel.tictactoe.server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class IpUtil {
    public static String resolveIPAddresses() {
        try {
            String ip = NetworkInterface.networkInterfaces()
                .flatMap(networkInterface -> toStream(networkInterface.getInetAddresses()))
                .filter(inetAddress -> !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                .map(InetAddress::getHostAddress)
                .collect(Collectors.joining(", "));
            return ip;
        } catch(SocketException e) {
            return "127.0.0.1";
        }
    }

    private static Stream<InetAddress> toStream(Enumeration<InetAddress> inetAddresses) {
        return stream(spliteratorUnknownSize(inetAddresses.asIterator(), Spliterator.ORDERED), false);
    }
}
