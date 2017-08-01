package com.cst14.im.tools;

public class SocketParams {
    private String host;

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    private int port;

    /**
     *
     * @param host
     * @param port
     *
     * usage references:
     *  public static final String host = "192.168.191.1";   wifi
     *          private String host = "172.22.71.144";
     *          private int port = 8080;
     */
    public SocketParams(String host, int port) {
        this.host = host;
        this.port = port;
    }
}