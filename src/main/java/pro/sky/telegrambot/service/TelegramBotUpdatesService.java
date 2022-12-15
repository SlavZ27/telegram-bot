package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.component.Command;

@Service
public class TelegramBotUpdatesService {
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
                Command command = Command.fromString(parserService.parseWord(message, 0));
                if (command == null) {
                    logger.debug("Method processUpdate don't detected command from = {}", message);
                    telegramBotSenderService.sendUnknownProcess(update);
                    return;
                }
                switch (command) {
                    case START:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        System.out.println(
                                "detected enter : " +
                                        idChat + " / " +
                                        update.message().from().username() + " / " +
                                        update.message().from().firstName() + " / " +
                                        update.message().from().lastName());
                        telegramBotSenderService.sendStart(update);
                        break;
                    case NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        notificationService.processUpdate(update);
                        break;
                    case TIME_ZONE:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        telegramBotSenderService.sendWhatYourTimeZone(update);
                        break;
                    case GET_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        notificationService.sendAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    case CLEAR_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        notificationService.clearAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    case ADMIN_GET_ALL_NOTIFICATION:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
                        notificationService.sendAllNotificationFromAdmin(update);
                        break;
                    case ADMIN_DELETE_NOTIFICATION_BY_ID:
                        logProcessUpdateDetectedValidMessageCommand(command.getTitle(), idChat);
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
                    Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTitle())) {
                logger.debug("Method processUpdate detected VARIABLE_EMPTY_CALLBACK_DATA_FOR_BUTTON in callbackQuery");
                return;
            }
            Long idChat = update.callbackQuery().from().id();
            String callbackQuery = update.callbackQuery().data();
            Command callbackQueryCommand = Command.fromString(parserService.parseWord(callbackQuery, 0));
            if (callbackQueryCommand == null) {
                logger.debug("Method processUpdate don't detected command from = {}", callbackQuery);
                telegramBotSenderService.sendUnknownProcess(update);
                return;
            }
            switch (callbackQueryCommand) {
                case CALENDAR:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand.getTitle(), idChat);
                    calendarService.processNext(update);
                    break;
                case NOTIFICATION:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand.getTitle(), idChat);
                    notificationService.saveNotificationTaskFromCallbackQueryWithoutMessage(update);
                    break;
                case TIME_ZONE:
                    logProcessUpdateDetectedValidCallbackQueryCommand(callbackQueryCommand.getTitle(), idChat);
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