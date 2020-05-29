package org.bptlab.cepta.config;

import picocli.CommandLine.Option;
import java.io.Serializable;

public class MongoConfig implements Serializable,Cloneable {

    @Option(
            names = {"--mongodb-host"},
            description = "Specifies the Database Host (ex. mongodb).")
    static String host = "mongodb";

    @Option(
            names = {"--mongodb-port"},
            description = "Specifies the port where the Database is running.")
    static int port = 27017;

    @Option(
            names = {"-mongodb", "--mongodb-database"},
            description = "Specifies the name of the Database.")
    static String name = "mongodb";

    @Option(
            names = {"-mu", "--mongodb-user"},
            description = "Specifies the user of the Database.")
    static String user = "root";

    @Option(
            names = {"-mpw", "--mongodb-password"},
            description = "Specifies the password of the Database.")
    static String password = "example";

    @Option(
            names = {"-mc", "--mongodb-connector"},
            description = "Specifies the connector to the Database (ex. jdbc).")
    static String connector = "jdbc";

    @Option(
            names = {"-mproto", "--mongodb-protocol"},
            description = "Specifies the protocol of the Database (ex. ).")
    static String protocol = "mongodb";

    public MongoConfig() {
    }

    public String getUrl() {
        return String.format("%s:%s://%s:%d/%s", connector, protocol, host, port, name);
    }

    public MongoConfig withConnector(String connector) {
        MongoConfig.connector = connector;
        return this;
    }

    public MongoConfig withProtocol(String protocol) {
        MongoConfig.protocol = protocol;
        return this;
    }

    public MongoConfig withHost(String host) {
        MongoConfig.host = host;
        return this;
    }

    public MongoConfig withPort(int port) {
        MongoConfig.port = port;
        return this;
    }

    public MongoConfig withName(String name) {
        MongoConfig.name = name;
        return this;
    }

    public MongoConfig withUser(String user) {
        MongoConfig.user = user;
        return this;
    }

    public MongoConfig withPassword(String password) {
        MongoConfig.password = password;
        return this;
    }

    public String getConnector() {
        return connector;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
