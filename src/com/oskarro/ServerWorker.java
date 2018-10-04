package com.oskarro;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;  // tworzymy Socket (klienta)
    private final Server server;        // tworzymy instancje serwera
    private String login = null;        // tworzymy miejsce na login
    private OutputStream outputStream;  // tworzymy zmienną wyjściową
    private HashSet<String> topicSet = new HashSet<>();     // tworzymy katalog(kolekcje) pokojów/grup

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
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {     // komenda wylogowania z chatu
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {     // komenda dołączania do chatu
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {       // komenda wysyłania wiadomości
                    // poniższa linijka łączy tokeny (poszczególne fragmenty wprowadzonej komendy) - bez tego można by wpisać tylko jeden wyraz jako wiadomość)
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {      // komenda dołączenia do grupy/pokoju
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {     // komenda opuszczenia pokoju/grupy
                    handleLeave(tokens);
                } else {
                    // gdy nie zostanie rozpoznany element cmd to wysyła informacje o błędzie do clienta (wiadomosc string)
                    String msg = "unknown commend: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();   // zamykanie połączenia
    }

    // metoda wykorzystywana do opuszczania pokoju/grupy
    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];       // przypisanie tokena do nazwy tematu
            topicSet.remove(topic);            // pokój jest usuwany z listy pokojów danego uzytkownika
        }
    }

    // funkcja odpowiada za sprawdzenie czy użytkownik należy do danej grupy (czy jest zainteresowany tym tematem)
    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    // metoda do obsługi komendy join (dołączanie do pokoju/grupy)
    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];       // przypisanie tokena do nazwy tematu
            topicSet.add(topic);            // pokój jest dodawany do pełnej listy pokojów
        }
    }

    // metoda do obsługi komendy msg
    // odpowiada za bezposrednie wysylanie wiadomosci od jednego uzytkownika do drugiego
    // format: "msg" "login" body...
    // format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];  // info o tym, do jakiej osoby wiadomość jest wysyłana
        String body = tokens[2];     // treść wiadomości

        // sprawdzenie pierwszego znaku - czy jest to #
        // funkcja odczytuje # jako odniesienie do pokoju/tematu
        boolean isTopic = sendTo.charAt(0) == '#';


        // pobierana jest cala kilka zalogowanych userow, a nastepnie na podstawie wprowadzonego w komendzie loginu wysylana jest wiadomosc
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker: workerList) {
            // jeśli pierwszy znak to # to funckja odnosi się do pokoju/tematu
            if (isTopic) {
                // jeśli użytkownik jest członkiem danej grupy to zostaje mu dostarczona ta wiadomość
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    // metoda zamykania połączenia klient-server
    // jeśli uzytkownika wyloguje się to nalezy usuwac konkretną instancje z workerList
    private void handleLogoff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        server.removeWorker(this);

        // wiadomość o zalogowaniu się użytkownika (do wszystkich userów)
        String onlineMsg = "offline " + login + "\n";
        for(ServerWorker worker: workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
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

                List<ServerWorker> workerList = server.getWorkerList();     // lista zalogowanych userów

                // pętla idzie przez wszystkich użytkowników i wysyła info o akutalnie połączonych userach
                for(ServerWorker worker: workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // wiadomość o zalogowaniu się użytkownika (do wszystkich userów)
                String onlineMsg = "online " + login + "\n";
                for(ServerWorker worker: workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                // wiadomość o błędnym zalogowaniu użytkownika
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    // wysyłanie wiadomości do użytkowników z informacją o zalogowaniu się nowego użytkownika
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }

}
