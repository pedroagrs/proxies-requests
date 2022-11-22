package io.github.pedroagrs.requests.blacklist;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BlackListUtil {

    private final String[] INVALID_CHARS = new String[] {" ", "\"", "\\", ",", "\\/"};

    public String clearAddress(String address) {
        for (String replace : INVALID_CHARS) {
            address = address.replace(replace, "").split(",")[0].split(":")[0];
        }

        return address;
    }
}
