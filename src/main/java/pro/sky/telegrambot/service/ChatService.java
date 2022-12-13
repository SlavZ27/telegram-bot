package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.Chat;
import pro.sky.telegrambot.repository.ChatRepository;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private TelegramBotSenderService telegramBotSenderService;
    private final Logger logger = LoggerFactory.getLogger(ChatService.class);


    public Chat getChatByIdOrNew(Long id) {
        logger.info("Method getChatByIdOrNew was start for find Chat by id = {}, or return new Chat", id);
        Chat chat = chatRepository.getChatById(id);
        if (chat == null) {
            logger.debug("Method getChatByIdOrNew will return the new chat");
            chat = new Chat();
            chat.setId(id);
            chatRepository.save(chat);
            return chat;
        }
        logger.debug("Method getChatByIdOrNew will return the found chat");
        return chat;
    }

    public boolean isTimeZoneExist(Long id) {
        logger.info("Method isTimeZoneExist was start for check exist time zone from Chat with id = {}", id);
        Chat chat = findChat(id);
        if (chat == null || chat.getTimeZone() == null) {
            logger.debug("Method isTimeZoneExist will return false because chat with id = {} not found ", id);
            return false;
        }
        logger.debug("Method isTimeZoneExist will return true because chat with id = {} was found ", id);
        return true;
    }

    public Chat addChat(Long id) {
        Chat chat = new Chat();
        chat.setId(id);
        return addChat(chat);
    }

    public Chat addChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public Chat findChat(Long id) {
        return chatRepository.getChatById(id);
    }

    public void deleteChat(Long id) {
        Chat chat = new Chat();
        chat.setId(id);
        deleteChat(chat);
    }

    public void deleteChat(Chat chat) {
        chatRepository.delete(chat);
    }

    public void saveTimeZoneFromCallbackQuery(Update update) {
        String callbackQuery = update.callbackQuery().data();
        String[] callbackQueryMas = callbackQuery.split(" ");
        Long idChat = update.callbackQuery().from().id();
        logger.info("ChatId={}; Method saveTimeZoneFromCallbackQuery was start for save time zone for Chat", idChat);
        Chat chat = findChat(idChat);
        if (chat == null) {
            chat = new Chat();
            chat.setId(idChat);
        }
        chat.setTimeZone(Integer.parseInt(callbackQueryMas[1]));
        chatRepository.save(chat);
        logger.debug("ChatId={}; Method saveTimeZoneFromCallbackQuery set value time zone = {} for Chat", idChat, chat.getTimeZone());
        telegramBotSenderService.sendMessage(idChat, "Time zone successful");
        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public boolean checkAdmin(Long id) {
        logger.info("Method checkAdmin was start for check administrator privileges for Chat by id = {}", id);
        Chat chat = findChat(id);
        if (chat == null || !chat.isAdmin()) {
            logger.debug("Method checkAdmin was detected absence administrator privileges for Chat by id = {}", id);
            return false;
        }
        logger.debug("Method checkAdmin was detected presence administrator privileges for Chat by id = {}", id);
        return true;
    }

    public void saveTimeZone(Long id, int timeZone) {
        logger.info("Method saveTimeZone was start for set time zone value = {} for Chat by id = {}", timeZone, id);
        Chat chat = new Chat();
        chat.setId(id);
        chat.setTimeZone(timeZone);
        addChat(chat);
    }


}
