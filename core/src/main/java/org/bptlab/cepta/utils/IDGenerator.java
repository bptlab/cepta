package org.bptlab.cepta.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class IDGenerator {

    public static <T extends String>String hashed(T input) {
        return DigestUtils.md5Hex(input).toUpperCase();
    }

    public static <T extends Number>String hashed(T input) {
        return DigestUtils.md5Hex(String.valueOf(input)).toUpperCase();
    }

    public static String newCeptaID() {
        return String.format("CPTA-%s", hashed(UUID.randomUUID().toString()));
    }
}