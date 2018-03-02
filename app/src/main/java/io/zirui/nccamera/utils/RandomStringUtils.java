package io.zirui.nccamera.utils;

import java.util.UUID;

public class RandomStringUtils {

    public static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }
}