package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;

import java.util.List;

@Service
public class TelegramBotUpdatesService {

    private final static String MESSAGE_START = "I can process a welcome message";
    private final static String MESSAGE_UNKNOWN = "I don't know this command";

    private final static String COMMAND_START = "/start";
    private final static String MESSAGE_NOT_COMMAND = "Sorry. I can process only two command : " +
            COMMAND_START + " and " + NotificationService.COMMAND_NOTIFICATION + ", like '" +
            NotificationService.MESSAGE_NOTIFICATION_DEFAULT + "'";

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesService.class);
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationService notificationService;


    @Scheduled(fixedDelay = 60_000)
    private void sendActualNotifications() {
        List<NotificationTask> notificationTaskList = notificationService.getActualNotification();
        logger.info("Method sendActualNotifications was started for send {} notification(s)", notificationTaskList.size());


        notificationTaskList.forEach(notificationTask -> {
            Long idChat = notificationTask.getIdChat();
            String message = notificationTask.getTextMessage();
            logger.info("ChatId={}; Method sendActualNotifications process message : {}", idChat, message);
            sendMessage(idChat, message);
            notificationService.setNotificationComplete(notificationTask);
        });
        logger.info("Method sendActualNotifications completed processing notification(s)");
    }

    public void processUpdate(Update update) {
        if (update.message() == null) {
            return;
        }
        String message = update.message().text().trim();
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processUpdate was started for process a message : {}", idChat, message);
        if (message.startsWith("/")) {
            String command;
            if (message.contains(" ")) {
                command = message.substring(0, message.indexOf(" "));
            } else {
                command = message;
            }
            switch (command) {
                case COMMAND_START:
                    logger.info("ChatId={}; Method processUpdate detected '" + COMMAND_START + "'", idChat);
                    processStart(update);
                    break;
                case NotificationService.COMMAND_NOTIFICATION:
                    logger.info("ChatId={}; Method processUpdate detected '" +
                            NotificationService.COMMAND_NOTIFICATION + "'", idChat);
                    sendMessage(idChat,
                            notificationService.processNotification(update)
                    );
                    break;
                default:
                    logger.info("ChatId={}; Method processUpdate detected unknown command", idChat);
                    processUnknown(update);
                    break;
            }
        } else {
            logger.info("ChatId={}; Method processUpdate don't detected command", idChat);
            sendMessage(idChat, MESSAGE_NOT_COMMAND);
        }
    }

    private void processStart(Update update) {
        String firstName = update.message().from().firstName();
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processStart was started for send a welcome message", idChat);
        sendMessage(idChat, "Hello " + firstName + ". " + MESSAGE_START);
    }

    private void processUnknown(Update update) {
        String firstName = update.message().from().firstName();
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processUnknown was started for send a message about unknown command", idChat);
        sendMessage(idChat, "Sorry, " + firstName + ". " + MESSAGE_UNKNOWN);
    }


    private void sendMessage(Long idChat, String textMessage) {
        logger.info("ChatId={}; Method sendMessage was started for send a message : {}", idChat, textMessage);
        SendMessage sendMessage = new SendMessage(idChat, textMessage);
        SendResponse response = telegramBot.execute(sendMessage);
        if (response.isOk()) {
            logger.info("ChatId={}; Method sendMessage has completed sending the message", idChat);
        } else {
            logger.info("ChatId={}; Method sendMessage received an error : {}", idChat, response.errorCode());
        }
    }
}