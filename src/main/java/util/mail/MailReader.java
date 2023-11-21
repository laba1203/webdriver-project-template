package util.mail;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import util.UsersFactory;
import util.WaitFactory;
import util.log.Log;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MailReader {

    public static final String UNSEEN_FLAG = " [Unseen flag]";
    private static final String COMMON_GMAIL_ADDRESS = UsersFactory.getGmailUser();

    public MessageData getMessage(QueryDetails queryDetails) {
        return getMessage(COMMON_GMAIL_ADDRESS, queryDetails);
    }

    public MessageData getMessage(String user, QueryDetails queryDetails) {
        long waitTimeout = WaitFactory.EMAIL_RECEIVING_TIMEOUT;
        return getMessage(user, queryDetails, waitTimeout);
    }

    @SneakyThrows
    public MessageData getMessage(String user, QueryDetails queryDetails, long waitTimeout) {
        try (MailConnector mailBox = new MailConnector().connect(user)) {
            return waitTillMessageReceived(mailBox, queryDetails, waitTimeout);
        }
    }

    public MessageData waitTillMessageReceived(MailConnector mailBox, QueryDetails queryDetails, long waitSec) {
        String commonError = "Mail message with subject <" + queryDetails.getSubject() + "> was not found.";
        return waitTillMessageReceived(mailBox, queryDetails, waitSec, commonError);
    }

    @SneakyThrows
    public MessageData waitTillMessageReceived(MailConnector mailBox, QueryDetails queryDetails, long waitSec, String errorMsg) {
        Log.logRecord("Start waiting for email to be received. (Email subject: '" + queryDetails.getSubject() + "').");
        long sleepTime = 2000;
        int attempt = 0;
        long expectedAttempts = (waitSec * 1000) / sleepTime;

        while (attempt < expectedAttempts) {
            try (Folder openedFolder = mailBox.openInboxFolder()) {
                List<Message> messages = findMessage(openedFolder, queryDetails);
                if (!messages.isEmpty()) {
                    Log.debug("Email with subject '" + queryDetails.getSubject() + "' was received.");
                    Message message = messages.get(0);
                    markMessagesAsSeenInInbox(messages, openedFolder);
                    String html = MailReader.getHtmlMessageBody(message);
                    String sender = MailReader.getSenderEmail(message);

                    markAllEmailsAsSeenInInbox(openedFolder);

                    return new MessageData(html, sender);
                }
                Thread.sleep(sleepTime);
                Log.debug("Mail was not found. Attempt #" + attempt);
                attempt++;
            }
        }
        Log.logRecord(errorMsg);
        throw new TimeoutException(errorMsg);
    }

    @SneakyThrows
    private List<Message> findMessage(Folder folder, QueryDetails queryDetails) {
        Message matchedMsg = null;
        ArrayList<Message> messages = new ArrayList<>();
        for (Message message : getUnseenMessages(folder)) {
            Log.debug("getSubject: " + message.getSubject());
            Log.debug("getFrom: " + Arrays.toString(message.getFrom()));

            if ((queryDetails.getSender() == null && isMessageMatchedBySubject(message, queryDetails))
                    || isMessageMatchedBySubjectAndSender(message, queryDetails)) {
                matchedMsg = message;
            }
            if (matchedMsg != null) {
                messages.add(matchedMsg);
            }
        }
        return messages;
    }

    @SneakyThrows
    private Message[] getUnseenMessages(Folder folder) {
        FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        return folder.search(unseenFlagTerm);
    }

    @SneakyThrows
    private boolean isMessageMatchedBySubject(Message message, QueryDetails queryDetails) {
        return message.getSubject().contains(queryDetails.getSubject());
    }

    @SneakyThrows
    private boolean isMessageMatchedBySubjectAndSender(Message message, QueryDetails mailDetails) {
        return message.getSubject().contains(mailDetails.getSubject()) && Arrays.toString(message.getFrom()).contains(mailDetails.getSender());
    }

    @SneakyThrows
    private void markAllEmailsAsSeenInInbox(Folder openedInboxFolder) {
        Message[] unseenMsgs = getUnseenMessages(openedInboxFolder);
        int unseenCount = unseenMsgs.length;
        List<Message> unseenMsgsList = new ArrayList<>();
        for (Message message : unseenMsgs) {
            if (!message.getSubject().contains(UNSEEN_FLAG)) {
                unseenMsgsList.add(message);
            }
        }
        Message[] unseenMsgsWithoutFlag = unseenMsgsList.toArray(new Message[0]);
        openedInboxFolder.setFlags(unseenMsgsWithoutFlag, new Flags(Flags.Flag.SEEN), true);
        Log.logRecord("All unseen messages were marked as SEEN (count = " + unseenCount + ")");
    }

    @SneakyThrows
    public void markMessagesAsSeenInInbox(List<Message> messages, Folder openedInboxFolder) {
        int unseenCount = messages.size();
        Message[] m = new Message[unseenCount];
        openedInboxFolder.setFlags(messages.toArray(m), new Flags(Flags.Flag.SEEN), true);
        Log.logRecord("All unseen messages were marked as SEEN (count = " + unseenCount + ")");
    }

    @SneakyThrows
    public static String getHtmlMessageBody(Message msg) {
        if (msg.isMimeType("text/plain")) {
            return msg.getContent().toString();
        } else if (msg.isMimeType("multipart/*")) {
            String result = "";
            MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/html")) {
                    Log.debug("Message body is html");
                    result = (String) bodyPart.getContent();
                    Log.logRecord("Message body is parsed into String");
                }
            }
            return result;
        }
        return "";
    }

    public boolean isMessageReceived(String user, QueryDetails queryDetails) {
        return isMessageReceived(user, queryDetails, WaitFactory.EMAIL_RECEIVING_TIMEOUT);
    }

    public boolean isMessageReceived(String user, QueryDetails queryDetails, long timeout) {
        try {
            getMessage(user, queryDetails, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @SneakyThrows
    public static String getSenderEmail(Message message) {
        return StringUtils.substringBetween(Arrays.toString(message.getFrom()), "<", ">");
    }

    public static String findLinkInTheEmail(String htmlFromEmail, String text) {
        Elements elements = Jsoup.parse(htmlFromEmail).select("a[href]");
        String url = "";
        ArrayList<String> links = new ArrayList<>();
        for (Element el : elements) {
            links.add(el.attr("href"));
            if (el.text().equals(text)) {
                url = el.attr("href");
            }
        }
        Assert.assertFalse(url.isEmpty(),
                "Unsubscribe URL was not fount in the email. All links from email: " + Arrays.toString(links.toArray()));
        return url;
    }

    @SneakyThrows
    public void deleteEmails() {
        int minusDays = 2;
        LocalDate date = LocalDate.now().minusDays(minusDays);

        try (MailConnector mailBox = new MailConnector().connect(COMMON_GMAIL_ADDRESS);
             Folder openedFolder = mailBox.openInboxFolder()) {
            Message[] msg = openedFolder.getMessages();
            List<Message> toDeleteMsgsList = new ArrayList<>();

            Log.debug(msg.length + " size");

            int i = 0;
            while (new Date(msg[i].getSentDate().getTime()).toLocalDate().isBefore(date)) {
                toDeleteMsgsList.add(msg[i]);
                Log.debug("Message added to delete list, current count: " + (i + 1));
                i++;
            }
            Message[] toDeleteMsgs = toDeleteMsgsList.toArray(new Message[0]);
            int count = toDeleteMsgs.length;

            Log.debug(count + " messages were added to delete list.");
            Log.debug("Message deletion started.");

            openedFolder.setFlags(toDeleteMsgs, new Flags(Flags.Flag.DELETED), true);

            Log.logRecord("Messages older than " + minusDays + " days were deleted (count = " + count + ").");
        }
    }

    @Data
    public static class QueryDetails {
        private String subject;
        private String sender;

        public QueryDetails(String mailSubject, String mailSender) {
            subject = mailSubject;
            sender = mailSender;
        }

        public QueryDetails(String mailSubject) {
            subject = mailSubject;
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class MessageData {
        private final String html;
        private final String sender;

    }

}