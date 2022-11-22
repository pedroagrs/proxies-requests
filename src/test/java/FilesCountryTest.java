import io.github.pedroagrs.requests.protocol.decoder.ProtocolDecoderType;
import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import io.github.pedroagrs.requests.decoder.RequestProxyFileDecoderBuilder;
import io.github.pedroagrs.requests.protocol.socks.proxy.SocksCustomProxySelector;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class FilesCountryTest {

    private static final List<String> BLACKLIST_COUNTRIES =
            List.of("US", "CA", "BR", "HK", "RU", "DE", "FR", "IN", "AR", "IT", "NL", "ID", "KR", "PL", "GB", "SG", "AE", "CN", "BD");

    public static void main(String[] args) {
        new SocksRequestController(1000, () -> {
            Queue<SocksCustomProxySelector> proxies = RequestProxyFileDecoderBuilder.builder()
                    .file(new File("socks_proxies.txt"))
                    .decoderType(ProtocolDecoderType.HOST_AND_PORT_COUNTRY)
                    .countriesBlacklist(BLACKLIST_COUNTRIES)
                    .build()
                    .create()
                    .stream()
                    .map(customProxy -> new SocksCustomProxySelector(customProxy.getProxy(), customProxy.getId()))
                    .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll);

            if (proxies.isEmpty()) return null;

            return proxies;
        });
    }
}
