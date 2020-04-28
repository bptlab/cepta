package org.bptlab.cepta.utils;

import java.util.UUID;

public class IDGenerator {
    public static String newCeptaID() {
        return String.format("CPTA-%s", UUID.randomUUID().toString());
    }
}