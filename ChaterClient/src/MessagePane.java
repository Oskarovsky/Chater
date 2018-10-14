// panel do pisania/odbierania wiadomości od innych użytkowników

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener {

    private final ChaterClient client;
    private final String login;

    // inicjacja wybranego modelu
    private DefaultListModel<String> listModel = new DefaultListModel<>();

    // lista, która będzie przechowywała aktualne ostatnie wiadomości użytkowników (reprezentuje całą konwersacje)
    private JList<String> messageList = new JList<>(listModel);

    // pole do wpisywania wiadomości (reprezentuje text, który chcemy wyslac do uzytkownika)
    private JTextField inputField = new JTextField();

    // konstruktor
    public MessagePane(ChaterClient client, String login) {
        this.client = client;
        this.login = login;

        client.addMessageListener(this);

        // tworzenie GUI dla messagePane
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);   // dodaje możliwość wprowadzania tekstu

        // jeżeli użytkownik wprowadzi tekst i zatwierdzi to enterem to zostaje on wysłany do użytkownika
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();     // do zmiennej text zostaje przypisana wartość wpisana w polu tekstowym
                    client.msg(login, text);                // text wysyłamy jako wiadomość do serwera
                    listModel.addElement("You: " + text);             // wpisany tekst jest dodawany do listy konwersacji
                    inputField.setText("");                         // po potwierdzeniu wiadomości enterem - wpisany tekst do pola tekstowego zostaje usunięty
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


    }

    // funkcja zabrana ze stosu i nadpisana
    // obsługuje wysyłanie wiadomości (to co wyświetla się w oknie)
    @Override
    public void onMessage(String fromLogin, String msgBody) {
        // robimy filtr (segregacje) w celu sprawdzenia czy wiadomość idzie od dobrego usera
        // jeśli wiadomośc idzie od użytkownika, którego nie ma na liście userów to odrzucamy tą wiadomość
        if (login.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + msgBody;       // tworzymy wygląd wysłanej wiadomosci
            listModel.addElement(line);     // dodajemy wysłaną wiadomość do pełnej listy wiadomości (swojego rodzaju archiwum)
        }
    }
}
