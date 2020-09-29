package sample;

import java.io.Serializable;

public class Node implements Serializable {
    public int priority;
    public String ip;
    public boolean online;

    public Node(int priority, String ip) {
        this.priority = priority;
        this.ip = ip;
        this.online = true;
    }
}
