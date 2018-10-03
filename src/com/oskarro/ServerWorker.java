package com.oskarro;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;  // tworzymy Socket (klienta)
    private final Server server;        // tworzymy instancje serwera
    private String login = null;        // tworzymy miejsce na login
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // metoda, która obsługuje połączenie z klientem
    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();     // wysyłanie odpowiedzi z serwera do klienta (komunikacja zwrotna)

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));    // odczyt linia po linii
        String line;
        // jeśli znaleziono string 'quit' to zakoncz komunikacje
        while ( (line = reader.readLine()) != null) {
            // łączenie różnych wprowadzonych linii do jednego tokena
            String[] tokens = StringUtils.split(line);

            // dodane w celu potwierdzenia czy obsługiwane połączenia nie są wyjątkami
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                // deklaracja poszczególnych komend
                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else {
                    // gdy nie zostanie rozpoznany element cmd to wysyła informacje o błędzie do clienta (wiadomosc string)
                    String msg = "unknown commend: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();   // zamykanie połączenia
    }

    // pobiera login
    public String getLogin() {
        return login;
    }

    // metoda obsługująca wprowadzenie stringa 'login'
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        // stworzenie modelu logowania z poszczególnymi składanikami tokenu
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            // kiedy user się zaloguje to wysyłamy wszelkie informacje do serwera
            // obsługa przechowywania danych o użytkownikach
            // sprawdzanie danych usera
            if ((login.equals("guest") && password.equals("guest")) || login.equals("oskar") && password.equals("oskar")) {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in succesfully: " + login);     // wyświetla informacje o zalogowaniu

                String onlineMsg = "online " + login + "\n";    // wiadomość o zalogowaniu się użytkownika (do wszystkich userów)
                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker: workerList) {
                    worker.send(onlineMsg);
                }
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    // wysyłanie wiadomości do użytkowników z informacją o zalogowaniu się nowego użytkownika
    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

}
