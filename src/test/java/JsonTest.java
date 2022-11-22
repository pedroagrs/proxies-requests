import io.github.pedroagrs.requests.blacklist.BlackListController;
import io.github.pedroagrs.requests.blacklist.SimpleBlackListFactory;
import io.github.pedroagrs.requests.model.CustomProxy;
import io.github.pedroagrs.requests.protocol.http.HttpRequestController;
import io.github.pedroagrs.requests.protocol.socks.SocksRequestController;
import io.github.pedroagrs.requests.protocol.socks.proxy.SocksCustomProxySelector;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

public class JsonTest {
    private static final List<String> BLACKLIST_COUNTRIES = List.of(
            "US", "CA", "BR", "HK", "RU", "DE", "FR", "IN", "AR", "IT", "NL", "ID", "KR", "PL", "GB", "SG", "AE", "CN",
            "BD");

    private static final String HTTP_URL =
            "https://proxylist.geonode.com/api/proxy-list?limit=500&page=1&sort_by=lastChecked&sort_type=desc&protocols=http";

    private static final String SOCKS_URL =
            "https://proxylist.geonode.com/api/proxy-list?limit=500&page=1&sort_by=lastChecked&sort_type=desc&protocols=socks5";

    public static void main(String[] args) {

       new HttpRequestController(100, () -> applyBlacklist(getProxies()).stream()
                .filter(proxy -> proxy.getProxy().type() == Proxy.Type.HTTP)
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));

        new SocksRequestController(100, () -> applyBlacklist(getProxies()).stream()
                .filter(proxy -> proxy.getProxy().type() == Proxy.Type.SOCKS)
                .map(proxy -> new SocksCustomProxySelector(proxy.getProxy(), proxy.getId()))
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));
    }

    private static List<CustomProxy> getProxies() {
        final List<CustomProxy> proxies = new LinkedList<>();

        try {
            List<JSONObject> jsonObjects =
                    readJsonArrayFromUrl("https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list");

            jsonObjects.forEach(json -> {
                String host = json.getString("host");
                String country = json.get("country").toString();
                String protocol = json.getString("type");
                Proxy.Type type = protocol.equals("http") || protocol.equals("https") ? Proxy.Type.HTTP : Proxy.Type.SOCKS;

                int port = json.getInt("port");

                if (BLACKLIST_COUNTRIES.contains(country)) return;

                proxies.add(new CustomProxy(host + ":" + port, new Proxy(type, new InetSocketAddress(host, port))));
            });

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return proxies;
    }

    private static List<CustomProxy> applyBlacklist(List<CustomProxy> customProxies) {
        final int size = customProxies.size();

        SimpleBlackListFactory blackListFactory =
                BlackListController.getInstance().getFactory();

        customProxies.removeIf(customProxy -> blackListFactory.contains(customProxy.getId()));

        System.out.println("Blacklist applied, removed " + (size - customProxies.size()) + " proxies");

        return customProxies;
    }

    private static List<JSONObject> readJsonArrayFromUrl(String url) throws IOException {
        try (InputStream inputStream = new URL(url).openStream();
                Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return read(reader);
        }
    }

    private static List<JSONObject> read(Reader reader) throws IOException {
        List<JSONObject> json = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(line -> json.add(new JSONObject(line)));
        }

        return json;
    }
}
