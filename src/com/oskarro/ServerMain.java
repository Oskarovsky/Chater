package com.oskarro;

public class ServerMain {
    public static void main(String[] args) {
        // tworzenie network servera (początek chat-servera)
        int port = 8818;    // port serwera
        Server server = new Server(port);    // tworzenie instancji serwera
        server.start();     // uruchomienie pracy serwera

    }

}
