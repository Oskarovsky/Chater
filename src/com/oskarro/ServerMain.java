package com.oskarro;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class ServerMain {
    public static void main(String[] args) {
        // tworzenie network servera (początek chat-servera)
        int port = 8818;    // port serwera
        try {
            ServerSocket serverSocket = new ServerSocket(port); // tworzymy server socket, który jest potrzebny do nawiązania połączenia
            while(true) {
                System.out.println("About to accept client connection...");     // informacja o oczekiwaniu na klienta
                Socket clientSocket = serverSocket.accept();    // metoda, która odpowiada za połaczenie serwera z klientem (jesli nie będzie połączenia - jest ona blokowana)
                System.out.println("Accepted connection from " + clientSocket);     // informacja o poprawnej weryfikacji klienta (wyświetla informacje o połączeniu - port clienta/servera)


                // tworzenie nowego wątku za każdym razem, gdy nawiazywane jest połaczenie z klientem
                // dzieki temu może być podłączonych kilku klientów jednocześnie
                ServerWorker worker = new ServerWorker(clientSocket);
                worker.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
