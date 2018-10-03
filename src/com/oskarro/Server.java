package com.oskarro;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// konstruktor serwera
public class Server extends Thread {
    private final int serverPort;       // port serwera
    private ArrayList<ServerWorker> workerList = new ArrayList<>();     // lista wszystkich użytkowników


    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort); // tworzymy server socket, który jest potrzebny do nawiązania połączenia
            while(true) {
                System.out.println("About to accept client connection...");     // informacja o oczekiwaniu na klienta
                Socket clientSocket = serverSocket.accept();    // metoda, która odpowiada za połaczenie serwera z klientem (jesli nie będzie połączenia - jest ona blokowana)
                System.out.println("Accepted connection from " + clientSocket);     // informacja o poprawnej weryfikacji klienta (wyświetla informacje o połączeniu - port clienta/servera)


                // tworzenie nowego wątku za każdym razem, gdy nawiazywane jest połaczenie z klientem
                // dzieki temu może być podłączonych kilku klientów jednocześnie
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);     // dodawanie nowego usera do listy
                worker.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
