// budowa GUI
// wykorzystujemy do tego Java Swing GUI (zestaw narzędzi)

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {


    private final ChaterClient client;
    private JList<String> userListUI;      // pokazuje listę użytkowników
    private DefaultListModel<String> userListModel;

    // konstruktor
    // dodaje obecnego użytkownika do listy
    public UserListPane(ChaterClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);        // mówi nam czy użytkownik jest online czy offline

        // główny komponent GUI
        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        // kiedy użytkownik dwukrotnie kliknie na nazwę innego user'a - ta nazwa zostanie zapamiętana
        // i będzie wykorzystana do wybrania adresata wiadomości (otworzy nowe okno czatu)
        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, login);

                    // metoda odpowiedzialna na otwieranie nowego okna po dwukrotnym kliknięciu na nazwę użytkownika
                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500, 500);    // wymiary okna
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);   // dodaje nowe wiadomości do widocznego okna
                    f.setVisible(true);     // włącza widoczność okna
                }
            }
        });
    }

    public static void main(String[] args) {
        ChaterClient client = new ChaterClient("localhost", 8818);  // instancja konkretnego serwera

        UserListPane userListPane = new UserListPane(client);   // tworzenie listy użytkowników
        JFrame frame = new JFrame("User List:");        // tworzy nowe okno (lista użytkowników)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);        // wymiary okna

        frame.getContentPane().add(userListPane, BorderLayout.CENTER); // ustawiamy listę jako główny składnik tego okna
        frame.setVisible(true);     // włączamy widoczność okna

        // jeśli użytkownik połączy się z serwerem to może się zalogować
        if (client.connect()) {
            try {
                client.login("guest", "guest");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // kiedy użytkownik się zaloguje to chcemy dodać do niego model
    // pojawia sie wtedy napis z nazwą aktywnego użytkownika (zalogowanego)
    // istnieje kilka różnych implementacji modelu
    @Override
    public void online(String login) {
        userListModel.addElement(login);

    }

    // kiedy użytkownik wyloguje się to chcemy usunąć element modelu
    // znika wtedy napis z nazwą użytkownika, który opuścił czat (wylogował się)
    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
