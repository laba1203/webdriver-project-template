package util;

public class UsersFactory {

    public static String getGmailUser() {
        return PropertyLoader.loadCredentialProperty("gmail.user.email");
    }
}
