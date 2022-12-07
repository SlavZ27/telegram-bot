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
        if (update.message() != null) {
            String message = update.message().text().trim();
            Long idChat = update.message().chat().id();
            if (message.startsWith("/")) {
                String command = parserService.parseWord(message, 0);
                switch (command) {
                    case COMMAND_START:
                        logger.info("ChatId={}; Method processUpdate detected '" + COMMAND_START + "'", idChat);
                        telegramBotSenderService.sendStart(update);
                        break;
                    case NotificationService.COMMAND_NOTIFICATION:
                        logger.info("ChatId={}; Method processUpdate detected '" +
                                NotificationService.COMMAND_NOTIFICATION + "'", idChat);
                        notificationService.processNotification(update);
                        break;
                    case TimeZoneService.COMMAND_TIME_ZONE:
                        logger.info("ChatId={}; Method processUpdate detected '" +
                                TimeZoneService.COMMAND_TIME_ZONE + "'", idChat);
                        telegramBotSenderService.sendWhatYourTimeZone(update);
                        break;
                    case NotificationService.COMMAND_GET_ALL_NOTIFICATION:
                        notificationService.sendAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    case NotificationService.COMMAND_CLEAR_ALL_NOTIFICATION:
                        notificationService.clearAllNotification(update);
                        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
                        break;
                    default:
                        logger.info("ChatId={}; Method processUpdate detected unknown command", idChat);
                        telegramBotSenderService.sendUnknownProcess(update);
                        break;
                }
            } else {
                if (!notificationService.checkNotCompleteMessage(update)) {
                    logger.info("ChatId={}; Method processUpdate don't detected command", idChat);
                    telegramBotSenderService.sendSorryWhatICan(update);
                }
            }
        }
        if (update.callbackQuery() != null) {
            Long idChat = update.callbackQuery().from().id();
            String callbackQuery = update.callbackQuery().data();
            String callbackQueryCommand = parserService.parseWord(callbackQuery, 0);
            switch (callbackQueryCommand) {
                case CalendarService.COMMAND_CALENDAR:
                    logger.info("ChatId={}; Method processUpdate detected callbackQuery '" +
                            CalendarService.COMMAND_CALENDAR + "'", idChat);
                    calendarService.processNext(update);
                    break;
                case NotificationService.COMMAND_NOTIFICATION:
                    notificationService.saveNotificationTaskFromCallbackQueryWithoutMessage(update);
                    break;
                case TimeZoneService.COMMAND_TIME_ZONE:
                    chatService.saveTimeZoneFromCallbackQuery(update);
                    break;
            }
        }
    }

}