// GUI, który będzie odpowiedzialny za logowanie się użytkownika do czatu
// umożliwia to korzystanie z czatu przez kilku użytkowników

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame {
    private final ChaterClient client;
    JTextField loginField = new JTextField();               // pole do wprowadzenia loginu
    JPasswordField passwordField = new JPasswordField();    // pole do wprowadzenia hasła
    JButton loginButton = new JButton("Login");                     // przycisk do potwierdzenia logowania

    LoginWindow() {
        super("Login"); // nazwa okna - 'Login'

        this.client = new ChaterClient("localhost", 8818);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     // metoda decyduje co sie wydarzy po zamknięciu okna login (w nasyzm przypadku zamknie to program)

        // tworzymy layout
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));    // layout będzie korzystał z BoxLayout w położeniu vertical (Y)
        p.add(loginField);      // dodajemy pole loginu do okna
        p.add(passwordField);   // dodjamey pole hasła do okna
        p.add(loginButton);     // dodajemy przycisk do okna

        // logika działania po kliknięciu na przycisk
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);   // dodajemy ten panel do głównej zawartości tego okna
        pack();     // ta metoda dopasuje okna do pozostałych elementów i całej zawartości
        setVisible(true);       // metoda, która decyduje o widoczności okna
    }

    // metoda służąca do logowania się użytkownika do czatu
    private void doLogin() {
        String login = loginField.getText();        // zmienna login, która pobiera wartość pola loginField
        String password = passwordField.getText();  // zmienna password, która pobiera wartość pola passwordField

        try {
            if (client.login(login, password)) {
                // gdy zaloguje sie poprawnie to pokaż okno z listą zalogowanych użytkowników i wyłącz okno logowania
                UserListPane userListPane = new UserListPane(client);   // tworzenie listy użytkowników
                JFrame frame = new JFrame("User List:");        // tworzy nowe okno (lista użytkowników)
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 600);        // wymiary okna

                frame.getContentPane().add(userListPane, BorderLayout.CENTER); // ustawiamy listę jako główny składnik tego okna
                frame.setVisible(true);     // włączamy widoczność okna

                setVisible(false);
            } else {
                // pokaże błąd
                JOptionPane.showMessageDialog(this, "Invalid login or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);
    }
}
