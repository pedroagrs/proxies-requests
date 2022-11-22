import io.github.pedroagrs.requests.model.CustomProxy;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayDeque;
import java.util.Queue;

public class URLDecoderTest {

    private static final String URL = "https://proxy-list.org/english/index.php";

    public static void main(String[] args) {
        createCustomProxy(URL);
    }

    private static Queue<CustomProxy> createCustomProxy(String url) {
        final Queue<CustomProxy> proxies = new ArrayDeque<>();

        Document document;
        Connection connection;

        try {
            ProxySelector.setDefault(null);

            connection = Jsoup.connect(url).proxy(Proxy.NO_PROXY).timeout(3000);
            document = connection.get();
        } catch (Exception exception) {
            return proxies;
        }

        Elements elements = document.normalise().getElementsByClass("proxy");

        System.out.println(elements);

        return proxies;
    }
}
