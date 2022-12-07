package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
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

    public Chat getChatByIdOrNew(Long id) {
        Chat chat = chatRepository.getChatById(id);
        if (chat == null) {
            chat = new Chat();
            chat.setId(id);
            chatRepository.save(chat);
            return chat;
        }
        return chat;
    }

    public boolean isTimeZoneExist(Long id) {
        Chat chat = findChat(id);
        return chat != null && chat.getTimeZone() != null;
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
        Chat chat = findChat(idChat);
        if (chat == null) {
            chat = new Chat();
            chat.setId(idChat);
        }
        chat.setTimeZone(Integer.parseInt(callbackQueryMas[1]));
        chatRepository.save(chat);
        telegramBotSenderService.sendMessage(idChat, "Time zone successful");
        telegramBotSenderService.sendMessage(idChat, TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    public Chat saveTimeZone(Long id, int timeZone) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setTimeZone(timeZone);
        addChat(chat);
        return chat;
    }


}
