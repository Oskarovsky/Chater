// interfejs do odbierania wiadomości (tworzenie listenera)

public interface MessageListener {
    // zawsze, gdy ktoś wyślę wiadomosć (np. gdy user wysyła wiadomość do serwera, a serwer przekazuje ją do innego usera)
    // fromLogin - od kogo przychodzi ja wiadomość, msgBody - jaka jest treść wiadomości
    public void onMessage(String fromLogin, String msgBody);
}
