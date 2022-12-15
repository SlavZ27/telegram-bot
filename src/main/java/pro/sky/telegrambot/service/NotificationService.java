package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.component.Command;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {
    public final static String MESSAGE_NOTIFICATION_DEFAULT = "/notification 01.01.2022 20:00 Сделать домашнюю работу";
    public final static String MESSAGE_BAD_REQUEST_NOTIFICATION = "Sorry. This request is bad. I need a request like : "
            + MESSAGE_NOTIFICATION_DEFAULT + ". And you can tap /notification";
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
    @Autowired
    private ParserService parserService;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void processUpdate(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processNotification was started for process update", idChat);

        if (!chatService.isTimeZoneExist(idChat)) {
            logger.info("ChatId={}; Method processNotification detected absence time zone for chat", idChat);
            telegramBotSenderService.sendWhatYourTimeZone(update);
            return;
        }

        if (update.message().text().length() == Command.NOTIFICATION.getTitle().length()
                && Command.NOTIFICATION.equals(Command.fromString(update.message().text()))) {
            logger.info("ChatId={}; Method processNotification detected '" + Command.NOTIFICATION +
                    "' without parameters and start calendar", idChat);
            calendarService.calendarStart(update, Command.NOTIFICATION);
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

        telegramBotSenderService.sendMessage(
                idChat,
                "Notification '" + createdNotification + "' is create");
        telegramBotSenderService.sendMessage(
                idChat,
                TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public boolean checkNotCompleteMessageAndComplete(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method checkNotCompleteMessageAndComplete was started for check unfinished Notification Task with only SYMBOL_EMPTY = ({}) ", idChat, SYMBOL_EMPTY);
        NotificationTask notificationTask =
                notificationTaskRepository.getFirstByTextMessageAndIdChatAndDone(idChat, SYMBOL_EMPTY);
        if (notificationTask != null) {
            saveMessageInNotificationTask(update, notificationTask);
            logger.debug("ChatId={}; Method checkNotCompleteMessageAndComplete was detected unfinished Notification and path", idChat);
            return true;
        } else {
            logger.debug("ChatId={}; Method checkNotCompleteMessageAndComplete was don't detected unfinished Notification", idChat);
            return false;
        }
    }

    public void sendAllNotification(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method sendAllNotification was started for send all notification by chat", idChat);
        List<NotificationTask> notificationTaskList = notificationTaskRepository.getAllByChat_IdUndeletedAndUndone(idChat);
        if (notificationTaskList.size() == 0) {
            telegramBotSenderService.sendMessage(idChat, "List is empty");
        } else {
            StringBuilder tempNotificationStr = new StringBuilder();
            for (int i = 0; i < notificationTaskList.size(); i++) {
                tempNotificationStr.append(notificationTaskList.get(i)).append("\n");
            }
            logger.debug("ChatId={}; Method sendAllNotification was detected {} notification", idChat, notificationTaskList.size());
            telegramBotSenderService.sendMessage(idChat, tempNotificationStr.toString());
        }
    }

    public void sendAllNotificationFromAdmin(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method sendAllNotificationFromAdmin was started for send all notification in all chats", idChat);
        if (chatService.checkAdmin(idChat)) {
            logger.debug("ChatId={}; Method sendAllNotificationFromAdmin detect admin", idChat);
            List<NotificationTask> notificationTaskList = notificationTaskRepository.getAll();
            if (notificationTaskList.size() == 0) {
                telegramBotSenderService.sendMessage(idChat, "List is empty");
            }
            StringBuilder tempNotificationStr = new StringBuilder();
            for (int i = 0; i < notificationTaskList.size(); i++) {
                tempNotificationStr.
                        append(notificationTaskList.get(i).getId()).append(" | ").
                        append(notificationTaskList.get(i).getChat().getId()).append(" | ").
                        append(notificationTaskList.get(i).getSender()).append(" | ").
                        append(notificationTaskList.get(i).getTextMessage()).append(" | ").
                        append(notificationTaskList.get(i).getLocalDateTime()).append(" | ");
                if (notificationTaskList.get(i).isDone()) {
                    tempNotificationStr.append("DONE");
                } else {
                    tempNotificationStr.append("NOT DONE");
                }
                tempNotificationStr.append(" | ");
                if (notificationTaskList.get(i).isDeleted()) {
                    tempNotificationStr.append("DELETED");
                } else {
                    tempNotificationStr.append("NOT DELETED");
                }
                tempNotificationStr.append("\n\n");
            }
            telegramBotSenderService.sendMessage(idChat, tempNotificationStr.toString());
        } else {
            logger.debug("ChatId={}; Method sendAllNotificationFromAdmin don't detect admin", idChat);
            telegramBotSenderService.sendSorryWhatICan(update);
        }
    }

    public void deleteNotificationById(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method deleteNotificationById was started for send all notification in all chats", idChat);
        if (chatService.checkAdmin(idChat)) {
            logger.debug("ChatId={}; Method deleteNotificationById detect admin", idChat);
            String message = parserService.parseWord(update.message().text().trim(), 1);
            long idParse;
            try {
                idParse = Long.parseLong(message);
            } catch (NumberFormatException e) {
                telegramBotSenderService.sendSorryWhatICan(update);
                return;
            }
            NotificationTask notificationTask = notificationTaskRepository.getNotificationTaskById(idParse);
            if (notificationTask == null) {
                telegramBotSenderService.sendMessage(idChat, "Not found");
                return;
            }
            notificationTaskRepository.delete(notificationTask);
            logger.info("ChatId={}; Method deleteNotificationById was deleted notification with id = {}", idChat, idParse);
            telegramBotSenderService.sendMessage(idChat, "DONE");
        } else {
            logger.debug("ChatId={}; Method deleteNotificationById don't detect admin", idChat);
            telegramBotSenderService.sendSorryWhatICan(update);
        }
    }

    public void clearAllNotification(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method clearAllNotification was started for soft clear all notification in chat", idChat);
        List<NotificationTask> notificationTaskList = notificationTaskRepository.getAllByChat_IdUndeleted(idChat);
        notificationTaskList.forEach(notificationTask -> {
            notificationTask.setDeleted(true);
            long tempId = notificationTask.getId();
            notificationTaskRepository.save(notificationTask);
            logger.debug("ChatId={}; Method clearAllNotification was soft deleted notification with id = {}", idChat, tempId);
        });
        logger.debug("ChatId={}; Method clearAllNotification was done", idChat);
        telegramBotSenderService.sendMessage(idChat, "Notifications deleted");
    }


    public void saveMessageInNotificationTask(Update update, NotificationTask notificationTask) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method saveMessageInNotificationTask was started for save message of notification in chat and complete notification task", idChat);
        notificationTask.setTextMessage(update.message().text());
        notificationTask.setDone(false);
        notificationTaskRepository.save(notificationTask);
        String createdNotification = notificationTask.getDateTime() + " " + notificationTask.getTextMessage();
        logger.info("ChatId={}; Method saveMessageInNotificationTask save request notification : {}", idChat, createdNotification);
        telegramBotSenderService.sendMessage(idChat, "Notification '" + createdNotification + "' is create");
        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public void saveNotificationTaskFromCallbackQueryWithoutMessage(Update update) {
        String callbackQuery = update.callbackQuery().data();
        String[] callbackQueryMas = callbackQuery.split(" ");
        Long idChat = update.callbackQuery().from().id();
        logger.info("ChatId={}; Method saveNotificationTaskFromCallbackQueryWithoutMessage was started for save notification without message", idChat);

        LocalDate localDate = LocalDate.of(
                Integer.parseInt(callbackQueryMas[2]),
                Integer.parseInt(callbackQueryMas[4]),
                Integer.parseInt(callbackQueryMas[6]));
        LocalTime localTime = LocalTime.of(
                Integer.parseInt(callbackQueryMas[8]),
                Integer.parseInt(callbackQueryMas[10]));
        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);

        if (chatService.findChat(idChat) == null) {
            logger.debug("ChatId={}; Method saveNotificationTaskFromCallbackQueryWithoutMessage don't found chat", idChat);
            telegramBotSenderService.sendMessage(idChat, "Error. Try again");
            return;
        }

        LocalDateTime localDateTime = getLocalDateTimeWithTimeZoneOfChat(idChat, localDate, localTime);

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
        logger.info("Method setNotificationComplete was started for set notification task with id = {} is complete", notificationTask.getId());
        notificationTask.setDone(true);
        notificationTaskRepository.save(notificationTask);
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkActualNotification() {
        LocalDateTime localDateTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime localDateTime2 = localDateTime1.plusSeconds(59);
        List<NotificationTask> notificationTaskList =
                notificationTaskRepository.findAllByDoneIsFalseAndDateTimeIsBetween(localDateTime1, localDateTime2);
        logger.info("Method checkActualNotification was found {} actual notification(s)", notificationTaskList.size());
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
        StringBuilder sb = new StringBuilder(update.message().text().substring(Command.NOTIFICATION.getTitle().length()).trim());
        if (sb.length() < 14 || sb.indexOf(" ") < 0) {
            return null;
        }
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method parseNotificationTaskFromUpdate was start for parse and return NotificationTask from Update", idChat);

        String[] words = new String[3];
        words[0] = sb.substring(0, sb.indexOf(" "));
        sb.delete(0, sb.indexOf(" ") + 1);

        if (sb.length() == 0) {
            return null;
        }
        while (sb.toString().startsWith(" ")) {
            sb.delete(0, 1);
        }
        if (sb.indexOf(" ") < 0) {
            return null;
        }

        words[1] = sb.substring(0, sb.indexOf(" "));
        sb.delete(0, sb.indexOf(" ") + 1);

        if (sb.length() == 0) {
            return null;
        }
        while (sb.toString().startsWith(" ")) {
            sb.delete(0, 1);
        }
        words[2] = sb.substring(0);

        LocalTime localTime = null;
        LocalDate localDate = null;
        String textMessage = null;
        int year = -1;
        int month = -1;
        int day = -1;
        int hour = -1;
        int minute = -1;


        for (String word : words) {
            if (localDate == null && StringUtils.containsOnly(word, ALPHABET_DATE)) {
                String[] numbersDate = word.split("\\.");
                if (numbersDate.length != 3) {
                    logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse date from request", idChat);
                    return null;
                }
                for (int i = 0; i < numbersDate.length; i++) {
                    try {
                        int tempInt = Integer.parseInt(numbersDate[i]);
                        if (day < 0 && tempInt > 0 && tempInt <= 31) {
                            day = tempInt;
                        } else if (month < 0 && tempInt > 0 && tempInt <= 12) {
                            month = tempInt;
                        } else if (year < 0 && tempInt > 12) {
                            year = tempInt;
                        }
                    } catch (NumberFormatException e) {
                        logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse date from request", idChat);
                        return null;
                    }
                }
                try {
                    localDate = LocalDate.of(year, month, day);
                } catch (DateTimeException e) {
                    logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse date from request", idChat);
                    return null;
                }
            } else if (localTime == null && StringUtils.containsOnly(word, ALPHABET_TIME)) {
                String[] numbersTime = word.split(":");
                if (numbersTime.length != 2) {
                    logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse time from request", idChat);
                    return null;
                }
                for (int i = 0; i < numbersTime.length; i++) {
                    try {
                        int tempInt = Integer.parseInt(numbersTime[i]);
                        if (hour < 0 && tempInt > 0 && tempInt <= 24) {
                            hour = tempInt;
                        } else if (minute < 0 && tempInt >= 0 && tempInt < 60) {
                            minute = tempInt;
                        }
                    } catch (NumberFormatException e) {
                        logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse time from request", idChat);
                        return null;
                    }
                }
                try {
                    localTime = LocalTime.of(hour, minute);
                } catch (DateTimeException e) {
                    logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse time from request", idChat);
                    return null;
                }
            } else if (textMessage == null) {
                textMessage = word;
            }
        }

        if (localTime == null || localDate == null) {
            logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate can't parse date or time from request", idChat);
            return null;
        }


        if (chatService.findChat(idChat) == null) {
            logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate don't found chat", idChat);
            return null;
        }

        LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);
        LocalDateTime localDateTime = getLocalDateTimeWithTimeZoneOfChat(idChat, localDate, localTime);

        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDateTime(dateTime);
        notificationTask.setLocalDateTime(localDateTime);
        notificationTask.setChat(chatService.getChatByIdOrNew(idChat));
        notificationTask.setTextMessage(textMessage);
        notificationTask.setDone(false);

        StringBuilder userName = new StringBuilder();
        if (update.message().
                from().
                username() != null) {
            userName.append(update.message().from().username());
            userName.append(" / ");
        }
        if (update.message().
                from().
                firstName() != null) {
            userName.append(update.message().from().firstName());
            userName.append(" / ");
        }
        if (update.message().
                from().
                username() != null) {
            userName.append(update.message().from().lastName());
        }
        notificationTask.setSender(userName.toString());
        logger.debug("ChatId={}; Method parseNotificationTaskFromUpdate done", idChat);
        return notificationTask;
    }

    private LocalDateTime getLocalDateTimeWithTimeZoneOfChat(Long idChat, LocalDate localDate, LocalTime localTime) {
        return LocalDateTime.of(localDate, localTime).plusHours(CalendarService.getLocalTimeZone()).
                minusHours(chatService.findChat(idChat).getTimeZone());
    }

}
