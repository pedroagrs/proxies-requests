import io.github.pedroagrs.requests.decoder.RequestProxyFileDecoderBuilder;
import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.http.HttpRequestController;
import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import io.github.pedroagrs.requests.protocol.socks.proxy.SocksCustomProxySelector;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayDeque;

public class FilesTest {

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
