package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramBotSenderService {

    private final static String MESSAGE_UNKNOWN = "I don't know this command";
    private final static String VARIABLE_EMPTY_SYMBOL_FOR_BUTTON = "...";
    public final static String VARIABLE_EMPTY_CALLBACK_DATA_FOR_BUTTON = "...";

    public final static String ALL_PUBLIC_COMMANDS = TelegramBotUpdatesService.COMMAND_START + "\n" +
            NotificationService.COMMAND_NOTIFICATION + "\n" +
            TimeZoneService.COMMAND_TIME_ZONE + "\n" +
            NotificationService.COMMAND_GET_ALL_NOTIFICATION + "\n" +
            NotificationService.COMMAND_CLEAR_ALL_NOTIFICATION;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotSenderService.class);
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private TimeZoneService timeZoneService;

    public void sendMessage(Long idChat, String textMessage) {
        logger.info("ChatId={}; Method sendMessage was started for send a message : {}", idChat, textMessage);
        SendMessage sendMessage = new SendMessage(idChat, textMessage);
        SendResponse response = telegramBot.execute(sendMessage);
        if (response.isOk()) {
            logger.debug("ChatId={}; Method sendMessage has completed sending the message", idChat);
        } else {
            logger.debug("ChatId={}; Method sendMessage received an error : {}", idChat, response.errorCode());
        }
    }

    public void sendUnknownProcess(Update update) {
        String firstName = update.message().from().firstName();
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processUnknown was started for send a message about unknown command", idChat);
        sendMessage(idChat, "Sorry, " + firstName + ". " + MESSAGE_UNKNOWN);
    }

    public void sendStart(Update update) {
        String firstName = update.message().from().firstName();
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processStart was started for send a welcome message", idChat);
        sendMessage(idChat, "Hello " + firstName + ".\n" +
                "I know some command:\n" + ALL_PUBLIC_COMMANDS);
    }

    public void sendSorryWhatICan(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method processWhatICan was started for send ability", idChat);
        sendMessage(idChat, "Sorry." + "\n" +
                "I know only this command:\n" + ALL_PUBLIC_COMMANDS);
    }

    public void sendButtons(Long idChat, String caption, String command, List<String> nameButtons, int width, int height) {
        logger.info("ChatId={}; Method sendButtons was started for send buttons", idChat);
        InlineKeyboardButton[][] tableButtons = new InlineKeyboardButton[height][width];
        int countNameButtons = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (countNameButtons < nameButtons.size()) {
                    tableButtons[i][j] = new InlineKeyboardButton(nameButtons.get(countNameButtons))
                            .callbackData(command + " " + nameButtons.get(countNameButtons));
                } else {
                    tableButtons[i][j] = new InlineKeyboardButton(VARIABLE_EMPTY_SYMBOL_FOR_BUTTON)
                            .callbackData(VARIABLE_EMPTY_CALLBACK_DATA_FOR_BUTTON);
                }
                countNameButtons++;
            }
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(tableButtons);
        SendMessage message = new SendMessage(idChat, caption).replyMarkup(inlineKeyboardMarkup);
        SendResponse response = telegramBot.execute(message);
        if (response.isOk()) {
            logger.debug("ChatId={}; Method sendButtons has completed sending the message", idChat);
        } else {
            logger.debug("ChatId={}; Method sendButtons received an error : {}", idChat, response.errorCode());
        }
    }

    public void sendWhatYourTimeZone(Update update) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method sendWhatYourTimeZone was started for ask about Time zone of user", idChat);
        sendButtons(idChat, "What your time zone?", TimeZoneService.COMMAND_TIME_ZONE, timeZoneService.getTimeZoneList(), 4, 6);
    }
}
