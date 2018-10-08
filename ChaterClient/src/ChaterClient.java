// indywidualny interfejs do połaczenia z serwerem


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChaterClient {

    private final String serverName;        // adres serwera
    private final int serverPort;           // port serwera
    private Socket socket;                  // Socket odpowiadający za połączenie
    private OutputStream serverOut;         // zmienna wyjściowa
    private InputStream serverIn;           // zmienna wejściowa
    private BufferedReader bufferedIn;      // zmienna buforowa do odczytu linii

    // lista użytkowników (listeners), którym wysyłana będzie informacja o online/offline
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    // lista wiadomości
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    // konstruktor
    public ChaterClient(String serverName, int serverPort) {
        this.serverName = serverName;       // adres serwera
        this.serverPort = serverPort;       // port serwera
    }

    // FUNKCJA TESTOWA - SPRAWDZA JAK DZIAŁAJĄ KOMPONENTY
    public static void main(String[] args) throws IOException {
        ChaterClient client = new ChaterClient("localhost", 8818);  // instancja konkretnego serwera
        // informuje o obecności użytkowników (online/offline)
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        // metoda do testowania MessageListener'a - przesyłanie wiadomości
        // funkcja, która jest wywoływana, gdy użytkownik napisze jakąś wiaodmość
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a messgae from " + fromLogin + " ===>" + msgBody);
            }
        });

        // informacja w razie braku połączenia klienta z serwerem lub w przypadku poprawnego połaczenia
        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            System.out.println("Connect successful");

            if (client.login("guest", "guest")) {
                System.out.println("Login successful");
                // Wysyłanie wiadomosći do użytkownika po zalogowaniu ("oskar" - odbiorca wiadomości, "Hello World" - treść wiadomości)
                client.msg("oskar", "Hello World!");
            } else {
                System.err.println("Login failed");
            }
            
            //client.logoff();
        }
    }

    // Metoda wysyłająca wiadomość do zalogowanego użytkownika (String odbiorca_wiadomosći, String tresc_wiadomosći)
    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    // komenda logoff dla klienta
    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    // metoda wyswietlajaca informacje o zalogowanym uzytkowniku
    // funckja sprawdza czy użytkownik jest zalogowany (return -> false/true)
    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);

        if ("ok login".equalsIgnoreCase(response)) {
            // rozpoczęcie odczytywania odpowiedzi z serwera
            startMessageReader();
            return true;
        } else {
            return false;
        }

    }

    // metoda, która odpowiada za odczytywanie kolejnych odpowiedzi ze strony serwera
    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    // metoda obsługująca odbiór wszystkich wiadomości ze strony serwera
    private void readMessageLoop() {
        try {
            String line;
            // pętla czyta każdą linię z wyjścia output ze strony serwera (powinien on być równy z inputem klienta)
            while ( (line = bufferedIn.readLine()) != null) {
                // łączenie różnych wprowadzonych linii do jednego tokena
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokenMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokenMsg);
                    }
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // funkcja obsługująca wysyłanie wiadomosći do użytkowników
    private void handleMessage(String[] tokenMsg) {
        String login = tokenMsg[1];
        String msgBody = tokenMsg[2];

        for(MessageListener listener: messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    // funkcja, która ponownie przywołuje wszystkich użytkoników offline
    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners) {
            listener.offline(login);
        }
    }

    // funkcja, która ponownie przywołuje wszystkich użytkowników (listeners online)
    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners) {
            listener.online(login);
        }
    }

    // funkcja odpowiadająca za połączenie klienta z serwerem
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());  // wyświetlanie informacji o aktualnym porcie używanym przez połączonego użytkownika
            this.serverOut = socket.getOutputStream();      // dane wyjściowe na serwer
            this.serverIn = socket.getInputStream();        // dane wejściowe do klienta
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // funkcja która rejestruje pozostałych użytkowników jako kolejnych listenerów (dotyczy sprawdzania statusu - offline/online)
    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    // funkcja, która odpowiada za usuwanie niekatywnych użytkowników z listy (tym, którzy nie oczekują na informacje o online/offline pozostałych użytkowników)
    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    // funkcja, która dodaje kolejne wiadomosći do listy wiadomości
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    // funkcja, która odpowiada za usuwanie wiadomości z listy wszystkich wiadomości
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
