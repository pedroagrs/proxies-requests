import io.github.pedroagrs.requests.decoder.RequestProxyFileDecoderBuilder;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.http.HttpRequestController;
import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import io.github.pedroagrs.requests.protocol.socks.proxy.SocksCustomProxySelector;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayDeque;

public class Test {

    private static final String REGEX = "^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
            + "|"
            + "localhost" // localhost
            + "|"
            + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip
            + ":"
            + "[0-9]{1,5}$";

    public static void main(String[] args) {
        new HttpRequestController(100, () -> RequestProxyFileDecoderBuilder.builder()
                .file(new File("C:\\Workspace\\Random\\http-requests\\src\\main\\resources\\http_proxies.txt"))
                .decoderType(ProtocolDecoderType.HOST_AND_PORT)
                .proxyType(Proxy.Type.HTTP)
                .build()
                .create()
                .stream()
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));

        new SocksRequestController(100, () -> RequestProxyFileDecoderBuilder.builder()
                .file(new File("C:\\Workspace\\Random\\http-requests\\src\\main\\resources\\socks_proxies.txt"))
                .decoderType(ProtocolDecoderType.HOST_AND_PORT)
                .proxyType(Proxy.Type.SOCKS)
                .build()
                .create()
                .stream()
                .map(proxy -> new SocksCustomProxySelector(proxy.getProxy(), proxy.getId()))
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));
    }
}
