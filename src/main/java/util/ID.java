package util;

import java.net.InetAddress;

public class ID {
    private String username;
    private InetAddress IP; // user's IP
    private int port; //user's port number for listening incoming request (this port served as the port in server)

    public ID(String username, InetAddress IP, int port){
        this.username = username;
        this.IP = IP;
        this.port = port;
    }

    public String getUsername() {return username;}
    public InetAddress getIP() {return IP;}
    public int getPort() {return port;}

    public void setUsername(String username) {this.username = username;}
    public void setIP(InetAddress IP) {this.IP = IP;}
    public void getPort(int port) {this.port = port;}


}
