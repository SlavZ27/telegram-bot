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
import org.apache.commons.lang3.StringUtils;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TelegramBotUpdatesService {

    private final static String MESSAGE_START = "I can process a welcome message";
    private final static String MESSAGE_UNKNOWN = "I don't know this command";
    private final static String MESSAGE_NOTIFICATION_DEFAULT = "/notification 01.01.2022 20:00 Сделать домашнюю работу";
    private final static String MESSAGE_BAD_REQUEST_NOTIFICATION = "Sorry. This request is bad. I need a request like : " + MESSAGE_NOTIFICATION_DEFAULT;
    private final static String COMMAND_START = "/start";
    private final static String COMMAND_NOTIFICATION = "/notification";
    private final static String ALPHABET_DATE = "0123456789.";
    private final static String ALPHABET_TIME = "0123456789:";
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesService.class);
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;


    @Scheduled(fixedDelay = 60_000)
    private void checkActualNotification() {
        LocalDateTime localDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime localDateTime2 = localDateTime1.plusSeconds(59);
        List<NotificationTask> notificationTaskList =
                notificationTaskRepository.findAllByDoneIsFalseAndDateTimeIsBetween(localDateTime1, localDateTime2);
        logger.info("Method checkActualNotification was started in {}, and found {} actual notification(s)", localDateTime1, notificationTaskList.size());
        if (notificationTaskList.size() > 0) {
            sendNotifications(notificationTaskList);
        }
    }

    private void sendNotifications(List<NotificationTask> notificationTaskList) {
        logger.info("Method sendNotification was started for send {} notification(s)", notificationTaskList.size());
        notificationTaskList.forEach(notificationTask -> {
            Long idChat = notificationTask.getIdChat();
            String message = notificationTask.getTextMessage();
            logger.info("ChatId={}; Method sendNotification process message : {}", idChat, message);
            sendMessage(idChat, message);
            notificationTask.setDone(true);
            notificationTaskRepository.save(notificationTask);
        });
        logger.info("Method sendNotification completed processing notification(s)");
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
                case COMMAND_NOTIFICATION:
                    logger.info("ChatId={}; Method processUpdate detected '" + COMMAND_NOTIFICATION + "'", idChat);
                    processNotification(update);
                    break;
                default:
                    logger.info("ChatId={}; Method processUpdate detected unknown command", idChat);
                    processUnknown(update);
                    break;
            }
        }
    }

    private void processNotification(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processNotification was started for process update", idChat);

        if (update.message().text().length() < COMMAND_NOTIFICATION.length()) {
            logger.info("ChatId={}; Method processNotification detected bad request notification : {}", idChat, update.message().text());
            sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION);
            return;
        }

        NotificationTask notificationTask = parseNotificationTaskFromUpdate(update);
        if (notificationTask == null) {
            logger.info("ChatId={}; Method parseNotificationTaskFromUpdate in processNotification can't parse this request notification : {}", idChat, update.message().text());
            sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION);
            return;
        }

        notificationTaskRepository.save(notificationTask);

        String createdNotification = notificationTask.getDateTime() + " " + notificationTask.getTextMessage();
        logger.info("ChatId={}; Method processNotification save request notification : {}", idChat, createdNotification);
        sendMessage(idChat, "Notification '" + createdNotification + "' is create");
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

    private NotificationTask parseNotificationTaskFromUpdate(Update update) {
        StringBuilder sb = new StringBuilder(update.message().text().substring(COMMAND_NOTIFICATION.length()).trim());
        Long idChat = update.message().chat().id();

        String[] words = new String[3];
        words[0] = sb.substring(0, sb.indexOf(" "));
        sb.delete(0, sb.indexOf(" ") + 1);

        words[1] = sb.substring(0, sb.indexOf(" "));
        sb.delete(0, sb.indexOf(" ") + 1);

        words[2] = sb.substring(0);

        LocalTime localTime = null;
        LocalDate localDate = null;
        String textMessage = null;

        for (String word : words) {
            if (localDate == null && StringUtils.containsOnly(word, ALPHABET_DATE)) {
                localDate = LocalDate.parse(word, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } else if (localTime == null && StringUtils.containsOnly(word, ALPHABET_TIME)) {
                localTime = LocalTime.parse(word, DateTimeFormatter.ofPattern("HH:mm"));
            } else if (textMessage == null) {
                textMessage = word;
            }
        }
        if (localTime == null || localDate == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);

        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDateTime(dateTime);
        notificationTask.setIdChat(idChat);
        notificationTask.setTextMessage(textMessage);
        notificationTask.setDone(false);

        return notificationTask;
    }
}