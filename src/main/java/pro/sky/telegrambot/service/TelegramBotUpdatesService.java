package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotUpdatesService {


    public final static String COMMAND_START = "/start";

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesService.class);
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TelegramBotSenderService telegramBotSenderService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ParserService parserService;
    @Autowired
    private ChatService chatService;

    public void processUpdate(Update update) {
        if (update == null) {
            logger.debug("Method processUpdate detected 'update == null'");
            return;
        }
        if (update.message() != null) {
            if (update.message().text() == null ||
                    update.message().from() == null ||
                    update.message().from().id() == null) {
                logger.debug("Method processUpdate detected null in message()");
                return;
            }
            String message = update.message().text().trim();
            Long idChat = update.message().chat().id();
            if (message.startsWith("/")) {
                String command = parserService.parseWord(message, 0);
                switch (command) {
                    case COMMAND_START:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        System.out.println(
                                "detected enter : " +
                                        idChat + " / " +
                                        update.message().from().username() + " / " +
                                        update.message().from().firstName() + " / " +
                                        update.message().from().lastName());
                        telegramBotSenderService.sendStart(update);
                        break;
                    case NotificationService.COMMAND_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        notificationService.processNotification(update);
                        break;
                    case TimeZoneService.COMMAND_TIME_ZONE:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        telegramBotSenderService.sendWhatYourTimeZone(update);
                        break;
                    case NotificationService.COMMAND_GET_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        notificationService.sendAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    case NotificationService.COMMAND_CLEAR_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        notificationService.clearAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    case NotificationService.COMMAND_ADMIN_GET_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        notificationService.sendAllNotificationFromAdmin(update);
                        break;
                    case NotificationService.COMMAND_ADMIN_DELETE_NOTIFICATION_BY_ID:
                        logProcessUpdateDetectedValidMessageCommand(command, idChat);
                        notificationService.deleteNotificationById(update);
                        break;
                    default:
                        logger.info("ChatId={}; Method processUpdate detected unknown command", idChat);
                        telegramBotSenderService.sendUnknownProcess(update);
                        break;
                }
            } else {
                if (!notificationService.checkNotCompleteMessageAndComplete(update)) {
                    logger.info("ChatId={}; Method processUpdate don't detected command", idChat);
                    telegramBotSenderService.sendSorryWhatICan(update);
                }
            }
        }
        if (update.callbackQuery() != null) {
            if (update.callbackQuery().data() == null ||
                    update.callbackQuery().from() == null ||
                    update.callbackQuery().from().id() == null) {
                logger.debug("Method processUpdate detected null in callbackQuery()");
                return;
            }
            if (update.callbackQuery().data().equals(
                    TelegramBotSenderService.VARIABLE_EMPTY_CALLBACK_DATA_FOR_BUTTON)) {
                logger.debug("Method processUpdate detected VARIABLE_EMPTY_CALLBACK_DATA_FOR_BUTTON in callbackQuery");
                return;
            }
            Long idChat = update.callbackQuery().from().id();
            String callbackQuery = update.callbackQuery().data();
            String callbackQueryCommand = parserService.parseWord(callbackQuery, 0);
            switch (callbackQueryCommand) {
                case CalendarService.COMMAND_CALENDAR:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand, idChat);
                    calendarService.processNext(update);
                    break;
                case NotificationService.COMMAND_NOTIFICATION:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand, idChat);
                    notificationService.saveNotificationTaskFromCallbackQueryWithoutMessage(update);
                    break;
                case TimeZoneService.COMMAND_TIME_ZONE:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand, idChat);
                    chatService.saveTimeZoneFromCallbackQuery(update);
                    break;
            }
        }
    }

    private void logProcessUpdateDetectedValidMessageCommand(String command, long idChat) {
        logger.info("ChatId={}; Method processUpdate detected command from message '" + command + "'", idChat);
    }

    private void logProcessUpdateDetectedValidCallbackQueryCommand(String command, long idChat) {
        logger.info("ChatId={}; Method processUpdate detected command from callbackQuery '" +
                command + "'", idChat);
    }

}