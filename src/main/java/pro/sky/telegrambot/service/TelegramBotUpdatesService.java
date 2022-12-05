package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import org.apache.commons.lang3.StringUtils;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class TelegramBotUpdatesService {

    private final static String MESSAGE_START = "I can process a welcome message";
    private final static String MESSAGE_UNKNOWN = "I don't know this command";
    private final static String MESSAGE_NOTIFICATION_DEFAULT = "/notification 01.01.2022 20:00 Сделать домашнюю работу";
    private final static String MESSAGE_BAD_REQUEST_NOTIFICATION = "Sorry. This request is bad. I need a request like: ";
    private final static String COMMAND_START = "/start";
    private final static String COMMAND_NOTIFICATION = "/notification";
    private final static String ALPHABET_DATE = "0123456789.";
    private final static String ALPHABET_TIME = "0123456789:";
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesService.class);
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

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
                    if (message.length() > command.length()) {
                        NotificationTask notificationTask = parseNotificationTaskFromUpdate(update);
                        if (notificationTask == null) {
                            sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION + MESSAGE_NOTIFICATION_DEFAULT);
                            return;
                        }
                        notificationTaskRepository.save(notificationTask);
                        sendMessage(idChat, "OK");
                    } else {
                        sendMessage(idChat, MESSAGE_BAD_REQUEST_NOTIFICATION + MESSAGE_NOTIFICATION_DEFAULT);
                    }
                    break;
                default:
                    logger.info("ChatId={}; Method processUpdate detected unknown command", idChat);
                    processUnknown(update);
                    break;
            }
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
            if (localDate == null & StringUtils.containsOnly(word, ALPHABET_DATE)) {
                localDate = LocalDate.parse(word, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } else if (localTime == null & StringUtils.containsOnly(word, ALPHABET_TIME)) {
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

        return notificationTask;
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

}


/*    Update{
        update_id=424255724,
        message=Message{
            message_id=1,
            from=User{
                id=395597530,
                is_bot=false,
                first_name='Just need money',
                last_name='for',
                username='SlSvsv',
                language_code='en',
                can_join_groups=null,
                can_read_all_group_messages=null,
                supports_inline_queries=null
                },
            sender_chat=null,
            date=1670236961,
            chat=Chat{
                id=395597530,
                type=Private,
                first_name='Just need money',
                last_name='for', username='SlSvsv',
                title='null',
                photo=null,
                bio='null',
                has_private_forwards=null,
                description='null',
                invite_link='null',
                pinned_message=null,
                permissions=null,
                slow_mode_delay=null,
                message_auto_delete_time=null,
                has_protected_content=null,
                sticker_set_name='null',
                can_set_sticker_set=null,
                linked_chat_id=null,
                location=null
                },
            forward_from=null,
            forward_from_chat=null,
            forward_from_message_id=null,
            forward_signature='null',
            forward_sender_name='null',
            forward_date=null,
            is_automatic_forward=null,
            reply_to_message=null,
            via_bot=null,
            edit_date=null,
            has_protected_content=null,
            media_group_id='null',
            author_signature='null',
            text='/start',
            entities=[MessageEntity{
                type=bot_command,
                offset=0,
                length=6,
                url='null',
                user=null,
                language='null'
                }],
            caption_entities=null,
            audio=null,
            document=null,
            animation=null,
            game=null,
            photo=null,
            sticker=null,
            video=null,
            voice=null,
            video_note=null,
            caption='null',
            contact=null,
            location=null,
            venue=null,
            poll=null,
            dice=null,
            new_chat_members=null,
            left_chat_member=null,
            new_chat_title='null',
            new_chat_photo=null,
            delete_chat_photo=null,
            group_chat_created=null,
            supergroup_chat_created=null,
            channel_chat_created=null,
            message_auto_delete_timer_changed=null,
            migrate_to_chat_id=null,
            migrate_from_chat_id=null,
            pinned_message=null,
            invoice=null,
            successful_payment=null,
            connected_website='null',
            passport_data=null,
            proximity_alert_triggered=null,
            voice_chat_started=null,
            voice_chat_ended=null,
            voice_chat_participants_invited=null,
            voice_chat_scheduled=null,
            reply_markup=null
            },
        edited_message=null,
        channel_post=null,
        edited_channel_post=null,
        inline_query=null,
        chosen_inline_result=null,
        callback_query=null,
        shipping_query=null,
        pre_checkout_query=null,
        poll=null,
        poll_answer=null,
        my_chat_member=null,
        chat_member=null,
        chat_join_request=null
        }
 */