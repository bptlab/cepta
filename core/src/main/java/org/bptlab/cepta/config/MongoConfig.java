package org.bptlab.cepta.config;

import picocli.CommandLine.Option;

import java.io.Serializable;

public class MongoConfig implements Serializable,Cloneable {

    @Option(
            names = {"--db-host"},
            description = "Specifies the Database Host (ex. mongodb).")
    static String host = "mongodb";

    @Option(
            names = {"--db-port"},
            description = "Specifies the port where the Database is running.")
    )
}
