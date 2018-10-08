// interfejs - służący do obsługi wiadomości offline/online użytkowników od serwera

public interface UserStatusListener {
    public void online(String login);   // metoda do identyfikacji użytkowników, którzy są online
    public void offline(String login);  // metoda do identyfikacji użytkowników, którzy są offline
}
