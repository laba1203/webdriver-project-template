package util.mail;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.SneakyThrows;
import util.PropertyLoader;
import util.log.Log;

import java.io.Closeable;
import java.util.Objects;
import java.util.Properties;

public class MailConnector implements Closeable {

    private static final String INBOX_FOLDER = "inbox";
    private static final String SESSION_TYPE = "imap";
    private static final String DEFAULT_HOST = "imap.gmail.com";
    private static final String PASSWORD = PropertyLoader.loadCredentialProperty("gmail.user.password");

    private Store store;

    @SneakyThrows
    @Override
    public void close() {
        if (store != null) {
            store.close();
            Log.logInConsole("Disconnected from the mailbox");
        }
    }

    private Store getStore() {
        return Objects.requireNonNull(store);
    }

    public MailConnector connect(String user) throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session session = Session.getInstance(props);
        store = session.getStore(SESSION_TYPE);
        store.connect(DEFAULT_HOST, user, PASSWORD);
        Log.logRecord("Connected to the mailbox of the user: " + user);
        return this;
    }

    public Folder openInboxFolder() throws MessagingException {
        String folderName = INBOX_FOLDER;
        Folder inbox = getStore().getFolder(folderName);
        assert inbox != null;
        try {
            inbox.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            Log.logInConsole("Exception during opening of the folder: " + e.getMessage());
            e.printStackTrace();
            inbox.open(Folder.READ_WRITE);
        }
        Log.logInConsole("Folder '" + folderName + "' is opened in the mailbox");
        return inbox;
    }

}