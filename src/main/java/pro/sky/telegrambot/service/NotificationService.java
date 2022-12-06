package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

    public final static String COMMAND_NOTIFICATION = "/notification";
    public final static String MESSAGE_NOTIFICATION_DEFAULT = "/notification 01.01.2022 20:00 Сделать домашнюю работу";
    public final static String MESSAGE_BAD_REQUEST_NOTIFICATION = "Sorry. This request is bad. I need a request like : "
            + MESSAGE_NOTIFICATION_DEFAULT;
    private final static String ALPHABET_DATE = "0123456789.";
    private final static String ALPHABET_TIME = "0123456789:";

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public String processNotification(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processNotification was started for process update", idChat);

        if (update.message().text().length() < COMMAND_NOTIFICATION.length()) {
            logger.info("ChatId={}; Method processNotification detected bad request notification : {}", idChat, update.message().text());
            return MESSAGE_BAD_REQUEST_NOTIFICATION;
        }

        NotificationTask notificationTask = parseNotificationTaskFromUpdate(update);
        if (notificationTask == null) {
            logger.info("ChatId={}; Method parseNotificationTaskFromUpdate in processNotification can't parse this request notification : {}", idChat, update.message().text());
            return MESSAGE_BAD_REQUEST_NOTIFICATION;
        }

        notificationTaskRepository.save(notificationTask);

        String createdNotification = notificationTask.getDateTime() + " " + notificationTask.getTextMessage();
        logger.info("ChatId={}; Method processNotification save request notification : {}", idChat, createdNotification);
        return "Notification '" + createdNotification + "' is create";
    }

    public void setNotificationComplete(NotificationTask notificationTask) {
        notificationTask.setDone(true);
        notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> getActualNotification() {
        LocalDateTime localDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime localDateTime2 = localDateTime1.plusSeconds(59);
        List<NotificationTask> notificationTaskList =
                notificationTaskRepository.findAllByDoneIsFalseAndDateTimeIsBetween(localDateTime1, localDateTime2);
        logger.info("Method getActualNotification was started in {}, and found {} actual notification(s)", localDateTime1, notificationTaskList.size());
        return notificationTaskList;
    }

    private NotificationTask parseNotificationTaskFromUpdate(Update update) {
        StringBuilder sb = new StringBuilder(update.message().text().substring(COMMAND_NOTIFICATION.length()).trim());
        if (sb.length() < 14 || sb.indexOf(" ") < 0) {
            return null;
        }
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
        notificationTask.setSender(update.message().from().username());
        return notificationTask;
    }

}
