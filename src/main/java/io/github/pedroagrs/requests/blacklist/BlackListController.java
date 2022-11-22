package io.github.pedroagrs.requests.blacklist;

public class BlackListController {

    private static final BlackListController INSTANCE = new BlackListController();

    private final SimpleBlackListFactory blackListFactory;

    private BlackListController() {
        blackListFactory = new SimpleBlackListFactory();
    }

    public static BlackListController getInstance() {
        return INSTANCE;
    }

    public SimpleBlackListFactory getFactory() {
        return blackListFactory;
    }
}
