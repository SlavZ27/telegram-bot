package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    public final static String COMMAND_GET_ALL_NOTIFICATION = "/get_all_notification";
    public final static String COMMAND_CLEAR_ALL_NOTIFICATION = "/clear_all_notification";
    public final static String MESSAGE_NOTIFICATION_DEFAULT = "/notification 01.01.2022 20:00 Сделать домашнюю работу";
    public final static String MESSAGE_BAD_REQUEST_NOTIFICATION = "Sorry. This request is bad. I need a request like : "
            + MESSAGE_NOTIFICATION_DEFAULT;
    private final static String ALPHABET_DATE = "0123456789.";
    private final static String ALPHABET_TIME = "0123456789:";
    public final static String SYMBOL_EMPTY = "@";

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;
    @Autowired
    private TelegramBotSenderService telegramBotSenderService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ChatService chatService;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void processNotification(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processNotification was started for process update", idChat);

        if (!chatService.isTimeZoneExist(idChat)) {
            telegramBotSenderService.sendWhatYourTimeZone(update);
            return;
        }

        if (update.message().text().length() < COMMAND_NOTIFICATION.length()) {
            logger.info("ChatId={}; Method processNotification detected bad request notification : {}", idChat, update.message().text());
            telegramBotSenderService.sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION);
            return;
        }

        if (update.message().text().equals(COMMAND_NOTIFICATION)) {
            calendarService.calendarStart(update);
            return;
        }

        NotificationTask notificationTask = parseNotificationTaskFromUpdate(update);
        if (notificationTask == null) {
            logger.info("ChatId={}; Method parseNotificationTaskFromUpdate in processNotification can't parse this request notification : {}", idChat, update.message().text());
            telegramBotSenderService.sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION);
            return;
        }
        notificationTaskRepository.save(notificationTask);

        String createdNotification = notificationTask.getDateTime() + " " + notificationTask.getTextMessage();
        logger.info("ChatId={}; Method processNotification save request notification : {}", idChat, createdNotification);

        telegramBotSenderService.sendMessage(idChat, "Notification '" + createdNotification + "' is create");
        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public boolean checkNotCompleteMessage(Update update) {
        NotificationTask notificationTask =
                notificationTaskRepository.getFirstByTextMessageAndAndDone(SYMBOL_EMPTY);
        if (notificationTask != null) {
            saveMessageInNotificationTask(update, notificationTask);
            return true;
        } else {
            return false;
        }
    }

    public void sendAllNotification(Update update) {
        Long idChat = update.message().chat().id();
        List<NotificationTask> notificationTaskList = notificationTaskRepository.getAllByChat_Id(idChat);
        if (notificationTaskList.size() == 0) {
            telegramBotSenderService.sendMessage(idChat, "List is empty");
        } else {
            StringBuilder tempNotificationStr = new StringBuilder();
            for (int i = 0; i < notificationTaskList.size(); i++) {
                tempNotificationStr.append(notificationTaskList.get(i)).append("\n");
            }
            telegramBotSenderService.sendMessage(idChat, tempNotificationStr.toString());

        }
    }

    public void clearAllNotification(Update update) {
        Long idChat = update.message().chat().id();
        List<NotificationTask> notificationTaskList = notificationTaskRepository.getAllByChat_Id(idChat);
        notificationTaskRepository.deleteAll(notificationTaskList);
        telegramBotSenderService.sendMessage(idChat, "Notifications deleted");
    }


    public void saveMessageInNotificationTask(Update update, NotificationTask notificationTask) {
        Long idChat = update.message().chat().id();
        notificationTask.setTextMessage(update.message().text());
        notificationTask.setDone(false);
        notificationTaskRepository.save(notificationTask);

        String createdNotification = notificationTask.getDateTime() + " " + notificationTask.getTextMessage();
        logger.info("ChatId={}; Method processNotification save request notification : {}", idChat, createdNotification);
        telegramBotSenderService.sendMessage(idChat, "Notification '" + createdNotification + "' is create");
        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public void saveNotificationTaskFromCallbackQueryWithoutMessage(Update update) {
        String callbackQuery = update.callbackQuery().data();
        String[] callbackQueryMas = callbackQuery.split(" ");
        Long idChat = update.callbackQuery().from().id();

        LocalDate localDate = LocalDate.of(
                Integer.parseInt(callbackQueryMas[2]),
                Integer.parseInt(callbackQueryMas[4]),
                Integer.parseInt(callbackQueryMas[6]));
        LocalTime localTime = LocalTime.of(
                Integer.parseInt(callbackQueryMas[8]),
                Integer.parseInt(callbackQueryMas[10]));
        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);

        if (chatService.findChat(idChat) == null) {
            telegramBotSenderService.sendMessage(idChat, "Error. Try again");
            return;
        }

        LocalDateTime localDateTime = dateTime.
                plusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).
                minusHours(chatService.findChat(idChat).getTimeZone());

        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDateTime(dateTime);
        notificationTask.setLocalDateTime(localDateTime);
        notificationTask.setChat(chatService.getChatByIdOrNew(idChat));
        notificationTask.setTextMessage(SYMBOL_EMPTY);
        notificationTask.setDone(true);

        StringBuilder userName = new StringBuilder();
        if (update.callbackQuery().from().username() != null) {
            userName.append(update.callbackQuery().from().username());
            userName.append(" / ");
        }
        if (update.callbackQuery().from().firstName() != null) {
            userName.append(update.callbackQuery().from().firstName());
            userName.append(" / ");
        }
        if (update.callbackQuery().from().username() != null) {
            userName.append(update.callbackQuery().from().lastName());
        }

        notificationTask.setSender(userName.toString());

        notificationTaskRepository.save(notificationTask);

        telegramBotSenderService.sendMessage(idChat, "Write text of notification");
    }

    public void setNotificationComplete(NotificationTask notificationTask) {
        notificationTask.setDone(true);
        notificationTaskRepository.save(notificationTask);
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkActualNotification() {
        LocalDateTime localDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime localDateTime2 = localDateTime1.plusSeconds(59);
        List<NotificationTask> notificationTaskList =
                notificationTaskRepository.findAllByDoneIsFalseAndDateTimeIsBetween(localDateTime1, localDateTime2);
        logger.info("Method checkActualNotification was started in {}, and found {} actual notification(s)", localDateTime1, notificationTaskList.size());

        if (notificationTaskList.size() == 0) {
            return;
        }

        notificationTaskList.forEach(notificationTask -> {
            Long idChat = notificationTask.getChat().getId();
            String message = notificationTask.getTextMessage();
            logger.info("ChatId={}; Method sendActualNotifications process message : {}", idChat, message);
            telegramBotSenderService.sendMessage(idChat, "NOTIFICATION '" + message + "'");
            setNotificationComplete(notificationTask);
        });
        logger.info("Method checkActualNotification completed processing notification(s)");
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

        if (chatService.findChat(idChat) == null) {
            return null;
        }

        LocalDateTime localDateTime = dateTime.
                plusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).
                minusHours(chatService.findChat(idChat).getTimeZone());

        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDateTime(dateTime);
        notificationTask.setLocalDateTime(localDateTime);
        notificationTask.setChat(chatService.getChatByIdOrNew(idChat));
        notificationTask.setTextMessage(textMessage);
        notificationTask.setDone(false);

        StringBuilder userName = new StringBuilder();
        if (update.message().from().username() != null) {
            userName.append(update.message().from().username());
            userName.append(" / ");
        }
        if (update.message().from().firstName() != null) {
            userName.append(update.message().from().firstName());
            userName.append(" / ");
        }
        if (update.message().from().username() != null) {
            userName.append(update.message().from().lastName());
        }

        notificationTask.setSender(userName.toString());
        return notificationTask;
    }

}
