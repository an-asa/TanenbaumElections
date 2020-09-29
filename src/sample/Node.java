package sample;

public class Node {
    public int priority;
    public String ip;
    public boolean online;

    public Node(int priority, String ip) {
        this.priority = priority;
        this.ip = ip;
        this.online = true;
    }
}
