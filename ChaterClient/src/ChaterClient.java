// indywidualny interfejs do połaczenia z serwerem


import java.io.*;
import java.net.Socket;

public class ChaterClient {

    private final String serverName;        // adres serwera
    private final int serverPort;           // port serwera
    private Socket socket;                  // Socket odpowiadający za połączenie
    private OutputStream serverOut;         // zmienna wyjściowa
    private InputStream serverIn;           // zmienna wejściowa
    private BufferedReader bufferedIn;      // zmienna buforowa do odczytu linii

    // konstruktor
    public ChaterClient(String serverName, int serverPort) {
        this.serverName = serverName;       // adres serwera
        this.serverPort = serverPort;       // port serwera
    }

    public static void main(String[] args) throws IOException {
        ChaterClient client = new ChaterClient("localhost", 8818);  // instancja konkretnego serwera
        // informacja w razie braku połączenia klienta z serwerem lub w przypadku poprawnego połaczenia
        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            System.out.println("Connect successful");
            client.login("guest", "guest");
        }
    }

    // metoda wyswietlajaca informacje o zalogowanym uzytkowniku
    private void login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);

    }

    // funkcja odpowiadająca za połączenie klienta z serwerem
    private boolean connect() {
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
}
