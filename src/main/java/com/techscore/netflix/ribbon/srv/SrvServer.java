package com.techscore.netflix.ribbon.srv;

import java.util.Comparator;
import java.util.regex.Pattern;

import com.netflix.loadbalancer.Server;

public class SrvServer extends Server implements Comparable<SrvServer> {
    private String service;
    private String name;
    private String protocol;
    private int priority;
    private int weight;

    public SrvServer(String fqn, int priority, int weight, int port, String target) {
        super(LAST_DOT.matcher(target).replaceFirst(""), port);
        setFqn(fqn);
        this.priority = priority;
        this.weight = weight;
    }

    public SrvServer(String service, String protocol, String name, int priority, int weight, int port, String target) {
        super(LAST_DOT.matcher(target).replaceFirst(""), port);
        setZone(service);
        this.protocol = protocol;
        this.name = name;
        this.priority = priority;
        this.weight = weight;
    }

    public String getFqn() {
        return '_' + service + "._" + protocol + '.' + name + '.';
    }

    private static final Pattern FIRST_DOT = Pattern.compile("^\\.");
    private static final Pattern LAST_DOT = Pattern.compile("\\.$");

    public void setFqn(String fqn) {
        String[] parts = fqn.split("\\.", 3);
        setService(FIRST_DOT.matcher(parts[0]).replaceFirst(""));
        setProtocol(FIRST_DOT.matcher(parts[1]).replaceFirst(""));
        setName(LAST_DOT.matcher(parts[2]).replaceFirst(""));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public static Comparator<SrvServer> DEFAULT_COMPARATOR = Comparator.comparingInt(SrvServer::getPriority)
            .thenComparingInt(SrvServer::getWeight);

    @Override
    public int compareTo(SrvServer other) {
        return DEFAULT_COMPARATOR.compare(this, other);
    }
}
