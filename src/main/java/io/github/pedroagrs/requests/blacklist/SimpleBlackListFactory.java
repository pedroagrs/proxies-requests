package io.github.pedroagrs.requests.blacklist;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleBlackListFactory {

    private final List<String> blacklist;

    private final File file = new File("blacklist.txt");

    public SimpleBlackListFactory() {
        this.blacklist = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                blacklist.add(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void add(String address) {
        address = BlackListUtil.clearAddress(address);

        if (blacklist.contains(address)) return;

        blacklist.add(address);

        file.setWritable(true);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(address);
            writer.write("\r\n");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean contains(String address) {
        return blacklist.contains(BlackListUtil.clearAddress(address));
    }
}
