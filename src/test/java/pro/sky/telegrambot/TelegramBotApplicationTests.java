package pro.sky.telegrambot;

import com.github.javafaker.Faker;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pro.sky.telegrambot.entity.Chat;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.repository.ChatRepository;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Profile("test")
@ExtendWith(SpringExtension.class)
class TelegramBotApplicationTests {

    @Autowired
    @MockBean
    private TelegramBot telegramBot;
    @Autowired
    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener;
    @Autowired
    @SpyBean
    private TelegramBotUpdatesService telegramBotUpdatesService;
    @Autowired
    @MockBean
    private TelegramBotSenderService telegramBotSenderService;
    @Autowired
    @SpyBean
    private ParserService parserService;
    @Autowired
    @SpyBean
    private CalendarService calendarService;
    @Autowired
    @SpyBean
    private ChatService chatService;
    @Autowired
    @SpyBean
    private NotificationService notificationService;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;
    @Autowired
    private ChatRepository chatRepository;
    private final Faker faker = new Faker();

    @AfterEach
    public void afterEach() {
        notificationTaskRepository.deleteAll();
        chatRepository.deleteAll();

    }

    @BeforeEach
    public void beforeAll() {
        notificationTaskRepository.deleteAll();
        chatRepository.deleteAll();
        for (int i = 0; i < 50; i++) {
            Update update = generateUpdateMessageWithReflection();
            Chat chat = mapUpdateToChat(update);
            NotificationTask notificationTask1 = mapUpdateToNotificationTask(update, chat);
            NotificationTask notificationTask2 = mapUpdateToNotificationTask(update, chat);
            chatRepository.save(chat);
            notificationTaskRepository.save(notificationTask1);
            notificationTaskRepository.save(notificationTask2);
        }
    }

    @Test
    void contextLoads() {
        assertThat(telegramBot).isNotNull();
        assertThat(telegramBotUpdatesListener).isNotNull();
        assertThat(telegramBotSenderService).isNotNull();
        assertThat(telegramBotUpdatesService).isNotNull();
        assertThat(parserService).isNotNull();
        assertThat(calendarService).isNotNull();
        assertThat(chatService).isNotNull();
        assertThat(notificationService).isNotNull();
    }

    @Test
    public void receivingCallbackQueryCommandCalendar() {
        int year = 2020;
        int month = 02;
        int day = 02;
        int hour = 02;

        List<Update> updateListWithCommandNotification = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + CalendarService.VARIABLE_YEAR + " " + year + " "
                        + CalendarService.VARIABLE_MONTH + " " + month + " "
                        + CalendarService.VARIABLE_DAY + " " + day + " "
                        + CalendarService.VARIABLE_HOUR + " " + hour
        ));
        Chat chat = mapUpdateToChat(updateListWithCommandNotification.get(0));
        chatRepository.save(chat);
        telegramBotUpdatesListener.process(updateListWithCommandNotification);

        updateListWithCommandNotification = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + CalendarService.VARIABLE_YEAR + " " + year + " "
                        + CalendarService.VARIABLE_MONTH + " " + month + " "
                        + CalendarService.VARIABLE_DAY + " " + day
        ));
        telegramBotUpdatesListener.process(updateListWithCommandNotification);

        updateListWithCommandNotification = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + CalendarService.VARIABLE_YEAR + " " + year + " "
                        + CalendarService.VARIABLE_MONTH + " " + month
        ));
        telegramBotUpdatesListener.process(updateListWithCommandNotification);

        updateListWithCommandNotification = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + CalendarService.VARIABLE_YEAR + " " + year
        ));
        telegramBotUpdatesListener.process(updateListWithCommandNotification);

        verify(telegramBotSenderService, times(4)).
                sendButtons(
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyList(),
                        anyInt(),
                        anyInt());
    }

    @Test
    public void receivingCallbackQueryCommandNotification() {
        int sizeOfRepository = notificationTaskRepository.findAll().size();

        List<Update> updateListWithCommandNotification = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_NOTIFICATION + " "
                        + CalendarService.VARIABLE_YEAR + " " + 2020 + " "
                        + CalendarService.VARIABLE_MONTH + " " + 02 + " "
                        + CalendarService.VARIABLE_DAY + " " + 20 + " "
                        + CalendarService.VARIABLE_HOUR + " " + 20 + " "
                        + CalendarService.VARIABLE_MINUTES + " " + 20
        ));
        Chat chat = mapUpdateToChat(updateListWithCommandNotification.get(0));
        chatRepository.save(chat);

        assertThat(notificationTaskRepository.findAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithCommandNotification);
        assertThat(notificationTaskRepository.findAll().size()).isEqualTo(sizeOfRepository + 1);

        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), "Write text of notification");
    }

    @Test
    public void receivingCallbackQueryCommandTimeZone() {
        int sizeOfRepository = chatRepository.findAll().size();

        List<Update> updateListWithCommandTimeZone = List.of(generateUpdateCallbackQueryWithReflection(
                "",
                "",
                "",
                50L,
                TimeZoneService.COMMAND_TIME_ZONE + " 5"
        ));

        Chat chat = mapUpdateToChat(updateListWithCommandTimeZone.get(0));

        assertThat(chatRepository.findAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithCommandTimeZone);
        assertThat(chatRepository.findAll().size()).isEqualTo(sizeOfRepository + 1);

        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), "Time zone successful");
        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), TelegramBotSenderService.ALL_PUBLIC_COMMANDS);

        telegramBotUpdatesListener.process(updateListWithCommandTimeZone);
        assertThat(chatRepository.findAll().size()).isEqualTo(sizeOfRepository + 1);

        verify(telegramBotSenderService, times(2)).sendMessage(chat.getId(), "Time zone successful");
        verify(telegramBotSenderService, times(2)).sendMessage(chat.getId(), TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    @Test
    public void receivingUpdateWithCommandOnlyNotification() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        NotificationTask notificationTaskRandom = notificationTaskRepository.getAll().stream().findFirst().orElse(null);
        assertThat(notificationTaskRandom).isNotNull();

        List<Update> updateListWithCommandAdminGetAllNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_NOTIFICATION
        ));
        Chat chat = mapUpdateToChat(updateListWithCommandAdminGetAllNotification.get(0));
        chat.setAdmin(true);
        chatRepository.save(chat);

        telegramBotUpdatesListener.process(updateListWithCommandAdminGetAllNotification);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).
                sendButtons(
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyList(),
                        anyInt(),
                        anyInt());
    }

    @Test
    public void receivingUpdateWithCommandAdminDeleteNotificationById() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        NotificationTask notificationTaskRandom = notificationTaskRepository.getAll().stream().findFirst().orElse(null);
        assertThat(notificationTaskRandom).isNotNull();

        List<Update> updateListWithCommandAdminGetAllNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_ADMIN_DELETE_NOTIFICATION_BY_ID + " " +
                        notificationTaskRandom.getId()
        ));
        Chat chat = mapUpdateToChat(updateListWithCommandAdminGetAllNotification.get(0));
        chat.setAdmin(true);
        chatRepository.save(chat);

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithCommandAdminGetAllNotification);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository - 1);

        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), "DONE");

        updateListWithCommandAdminGetAllNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_ADMIN_DELETE_NOTIFICATION_BY_ID + " 5"
        ));

        telegramBotUpdatesListener.process(updateListWithCommandAdminGetAllNotification);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository - 1);

        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), "Not found");
    }

    @Test
    public void receivingUpdateWithCommandAdminGetAllNotification() {
        notificationTaskRepository.deleteAll();
        chatRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            Update update = generateUpdateMessageWithReflection();
            Chat chat = mapUpdateToChat(update);
            NotificationTask notificationTask1 = mapUpdateToNotificationTask(update, chat);
            NotificationTask notificationTask2 = mapUpdateToNotificationTask(update, chat);
            chatRepository.save(chat);
            notificationTaskRepository.save(notificationTask1);
            notificationTaskRepository.save(notificationTask2);
        }
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithCommandAdminGetAllNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_ADMIN_GET_ALL_NOTIFICATION
        ));
        Chat chat = mapUpdateToChat(updateListWithCommandAdminGetAllNotification.get(0));
        chat.setAdmin(true);
        chatRepository.save(chat);

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(10);
        telegramBotUpdatesListener.process(updateListWithCommandAdminGetAllNotification);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(10);


        List<NotificationTask> notificationTaskList = notificationTaskRepository.getAll();
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
        verify(telegramBotSenderService, times(1)).sendMessage(chat.getId(), tempNotificationStr.toString());

        chat.setAdmin(false);
        chatRepository.save(chat);

        telegramBotUpdatesListener.process(updateListWithCommandAdminGetAllNotification);

        verify(telegramBotSenderService, times(1)).sendSorryWhatICan(updateListWithCommandAdminGetAllNotification.get(0));
    }

    @Test
    public void receivingUpdateWithUnknownCommand() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithUnknownCommand = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                -1L,
                "/fghjffert"
        ));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithUnknownCommand);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendUnknownProcess(updateListWithUnknownCommand.get(0));
    }

    @Test
    public void receivingUpdateWithUnknown() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithUnknownCommand = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                -1L,
                "fghjffert"
        ));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithUnknownCommand);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendSorryWhatICan(updateListWithUnknownCommand.get(0));
    }

    @Test
    public void receivingUpdateStartMessage() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithCommandStart = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                -1L,
                TelegramBotUpdatesService.COMMAND_START
        ));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithCommandStart);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendStart(updateListWithCommandStart.get(0));
    }

    @Test
    public void receivingUpdateNotificationWithoutTimeZoneMessage() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithCommandNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                1L,
                NotificationService.COMMAND_NOTIFICATION + " 01.01.2020 noti"
        ));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListWithCommandNotification);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendWhatYourTimeZone(updateListWithCommandNotification.get(0));
    }

    @Test
    public void receivingUpdateWithoutCommandAndPathNotCompleteMessage() {
        Update update = generateUpdateMessageWithReflection
                (
                        "",
                        "",
                        "",
                        50L,
                        ""
                );

        Chat chat = mapUpdateToChat(update);
        chatRepository.save(chat);
        NotificationTask notificationTaskNotFinished = mapUpdateToNotificationTask(update, chat);

        notificationTaskNotFinished.setDone(true);
        notificationTaskNotFinished.setTextMessage(NotificationService.SYMBOL_EMPTY);
        notificationTaskRepository.save(notificationTaskNotFinished);

        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListWithoutCommand = List.of(generateUpdateMessageWithReflection
                (
                        "",
                        "",
                        "",
                        50L,
                        "noti"
                ));

        telegramBotUpdatesListener.process(updateListWithoutCommand);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        NotificationTask notificationTaskPath = notificationTaskRepository.getAllByChat_IdUndeleted(50L).stream().findFirst().orElse(null);
        assertThat(notificationTaskPath).isNotNull();

        String createdNotification = notificationTaskPath.getDateTime() + " " + updateListWithoutCommand.get(0).message().text();
        verify(telegramBotSenderService, times(1)).sendMessage(
                notificationTaskPath.getChat().getId(),
                "Notification '" + createdNotification + "' is create");
        verify(telegramBotSenderService, times(1)).sendMessage(
                notificationTaskPath.getChat().getId(),
                TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    @Test
    public void receivingUpdateMessageGetAllNotification() {
        List<Update> updateListForGetAll = new ArrayList<>();
        updateListForGetAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForGetAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForGetAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForGetAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForGetAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));

        Chat chat = mapUpdateToChat(updateListForGetAll.get(0));
        chatRepository.save(chat);
        updateListForGetAll.forEach(update -> notificationTaskRepository.save(mapUpdateToNotificationTask(update, chat)));

        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateGetAllList = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_GET_ALL_NOTIFICATION));

        List<Update> updateGetAllEmptyCheck = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                51L,
                NotificationService.COMMAND_GET_ALL_NOTIFICATION));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateGetAllEmptyCheck);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        telegramBotUpdatesListener.process(updateGetAllList);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        List<NotificationTask> notificationTaskListSaved = notificationTaskRepository.getAllByChat_IdUndeleted(chat.getId());

        StringBuilder tempNotificationStr = new StringBuilder();

        for (int i = 0; i < notificationTaskListSaved.size(); i++) {
            tempNotificationStr.append(notificationTaskListSaved.get(i)).append("\n");
        }

        verify(telegramBotSenderService).sendMessage(
                updateGetAllEmptyCheck.get(0).message().chat().id(),
                "List is empty");

        verify(telegramBotSenderService).sendMessage(
                updateGetAllEmptyCheck.get(0).message().chat().id(),
                TelegramBotSenderService.ALL_PUBLIC_COMMANDS);

        verify(telegramBotSenderService).sendMessage(
                updateGetAllList.get(0).message().chat().id(),
                TelegramBotSenderService.ALL_PUBLIC_COMMANDS);

        verify(telegramBotSenderService).sendMessage(
                updateGetAllList.get(0).message().chat().id(),
                tempNotificationStr.toString());

    }

    @ParameterizedTest
    @MethodSource("paramsForReceivingUpdateMessageNotificationWithIncorrectRequestWithTimeZone")
    public void receivingUpdateMessageNotificationWithTimeZone(String request, boolean isCorrect) {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListNotification = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_NOTIFICATION + " " + request
        ));

        Chat chat = mapUpdateToChat(updateListNotification.get(0));
        chatRepository.save(chat);

        telegramBotUpdatesListener.process(updateListNotification);

        if (isCorrect) {
            assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository + 1);

            NotificationTask notificationTaskSaved = notificationTaskRepository.
                    getAllByChat_IdUndeleted(chat.getId()).stream().findFirst().orElse(null);
            assertThat(notificationTaskSaved).isNotNull();

            String createdNotification = notificationTaskSaved.getDateTime() + " " + notificationTaskSaved.getTextMessage();


            verify(telegramBotSenderService, times(1)).sendMessage(
                    chat.getId(),
                    "Notification '" + createdNotification + "' is create");

            verify(telegramBotSenderService, times(1)).sendMessage(
                    chat.getId(),
                    TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
        } else {
            assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
            verify(telegramBotSenderService, times(1)).sendMessage(
                    chat.getId(),
                    NotificationService.MESSAGE_BAD_REQUEST_NOTIFICATION);
        }
    }

    public static Stream<Arguments> paramsForReceivingUpdateMessageNotificationWithIncorrectRequestWithTimeZone() {
        return Stream.of(
                Arguments.of("gfjhkhk", false),
                Arguments.of("25.25.2020 noti", false),
                Arguments.of("12:12 noti", false),
                Arguments.of("32.05.2020 12:12 noti", false),
                Arguments.of("25.13.2020 12:12 noti", false),
                Arguments.of("25.05.2020 25:25 noti", false),
                Arguments.of("25.05.2020 25:61 noti", false),
                Arguments.of("fdhgfjk 25.05.2020 12:12", true),
                Arguments.of("12:12 25.05.2020 noti", true),
                Arguments.of("25.05.2020 12:12 noti", true)
        );
    }

    @Test
    public void receivingUpdateMessageTimeZone() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListTimeZone = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                TimeZoneService.COMMAND_TIME_ZONE
        ));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);
        telegramBotUpdatesListener.process(updateListTimeZone);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendWhatYourTimeZone(updateListTimeZone.get(0));
    }

    @Test
    public void receivingUpdateMessageClearAllNotification() {
        int sizeOfRepository = notificationTaskRepository.getAll().size();

        List<Update> updateListForClearAll = new ArrayList<>();
        updateListForClearAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForClearAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForClearAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForClearAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));
        updateListForClearAll.add(generateUpdateMessageWithReflection("", "", "", 50L, ""));

        Chat chat = mapUpdateToChat(updateListForClearAll.get(0));
        chatRepository.save(chat);
        updateListForClearAll.forEach(update -> notificationTaskRepository.save(mapUpdateToNotificationTask(update, chat)));

        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository + 5);

        List<Update> updateListNotificationWithCommandClearAll = List.of(generateUpdateMessageWithReflection(
                "",
                "",
                "",
                50L,
                NotificationService.COMMAND_CLEAR_ALL_NOTIFICATION
        ));


        telegramBotUpdatesListener.process(updateListNotificationWithCommandClearAll);
        assertThat(notificationTaskRepository.getAll().size()).isEqualTo(sizeOfRepository + 5);
        assertThat(notificationTaskRepository.getAll().stream().
                filter(notificationTask -> !notificationTask.isDeleted())
                .count()).isEqualTo(sizeOfRepository);

        verify(telegramBotSenderService, times(1)).sendMessage(
                chat.getId(),
                "Notifications deleted");
        verify(telegramBotSenderService, times(1)).sendMessage(
                chat.getId(),
                TelegramBotSenderService.ALL_PUBLIC_COMMANDS);
    }

    private Chat mapUpdateToChat(Update update) {
        Chat chat = new Chat();
        if (update.message() != null) {
            chat.setId(update.message().chat().id());
            chat.setTimeZone(5);
            chat.setAdmin(false);
        } else if (update.callbackQuery() != null) {
            chat.setId(update.callbackQuery().from().id());
            chat.setTimeZone(5);
            chat.setAdmin(false);
        }
        return chat;
    }


    private NotificationTask mapUpdateToNotificationTask(Update update, Chat chat) {
        NotificationTask notificationTask = new NotificationTask();
        if (update.message() != null) {
            notificationTask.setDateTime(LocalDateTime.now());
            notificationTask.setLocalDateTime(LocalDateTime.now());
            notificationTask.setTextMessage(update.message().text());
            notificationTask.setDone(false);
            notificationTask.setDeleted(false);
            notificationTask.setChat(chat);
            notificationTask.setSender(update.message().from().username());
        } else if (update.callbackQuery() != null) {
            notificationTask.setDateTime(LocalDateTime.now());
            notificationTask.setLocalDateTime(LocalDateTime.now());
            notificationTask.setTextMessage(update.callbackQuery().data());
            notificationTask.setDone(false);
            notificationTask.setDeleted(false);
            notificationTask.setChat(chat);
            notificationTask.setSender(update.callbackQuery().from().username());
        }
        return notificationTask;
    }

    private Update generateUpdateCallbackQueryWithReflection() {
        return generateUpdateCallbackQueryWithReflection("", "", "", -1L, "");
    }

    private Update generateUpdateCallbackQueryWithReflection(String username,
                                                             String firstName,
                                                             String lastName,
                                                             Long chatId,
                                                             String callbackQueryData) {
        username = generateNameIfEmpty(username);
        firstName = generateNameIfEmpty(firstName);
        lastName = generateNameIfEmpty(lastName);
        chatId = generateIdIfEmpty(chatId);
        callbackQueryData = generateMessageIfEmpty(callbackQueryData);

        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        User user = new User(0L);

        try {
            Field userNameField = user.getClass().getDeclaredField("username");
            userNameField.setAccessible(true);
            Field firstNameField = user.getClass().getDeclaredField("first_name");
            firstNameField.setAccessible(true);
            Field lastNameField = user.getClass().getDeclaredField("last_name");
            lastNameField.setAccessible(true);
            Field userId = user.getClass().getDeclaredField("id");
            userId.setAccessible(true);
            userNameField.set(user, username);
            firstNameField.set(user, firstName);
            lastNameField.set(user, lastName);
            userId.set(user, chatId);

            Field callbackUserField = callbackQuery.getClass().getDeclaredField("from");
            callbackUserField.setAccessible(true);
            Field callbackDataField = callbackQuery.getClass().getDeclaredField("data");
            callbackDataField.setAccessible(true);
            callbackUserField.set(callbackQuery, user);
            callbackDataField.set(callbackQuery, callbackQueryData);

            Field updateCallbackField = update.getClass().getDeclaredField("callback_query");
            updateCallbackField.setAccessible(true);
            updateCallbackField.set(update, callbackQuery);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return update;
    }

    private Update generateUpdateMessageWithReflection() {
        return generateUpdateMessageWithReflection("", "", "", -1L, "");
    }

    private Update generateUpdateMessageWithReflection(String username,
                                                       String firstName,
                                                       String lastName,
                                                       Long chatId,
                                                       String messageText) {
        username = generateNameIfEmpty(username);
        firstName = generateNameIfEmpty(firstName);
        lastName = generateNameIfEmpty(lastName);
        messageText = generateMessageIfEmpty(messageText);
        chatId = generateIdIfEmpty(chatId);

        Update update = new Update();
        Message message = new Message();
        com.pengrad.telegrambot.model.Chat chat = new com.pengrad.telegrambot.model.Chat();
        User user = new User(0L);

        try {
            Field userNameField = user.getClass().getDeclaredField("username");
            userNameField.setAccessible(true);
            Field firstNameField = user.getClass().getDeclaredField("first_name");
            firstNameField.setAccessible(true);
            Field lastNameField = user.getClass().getDeclaredField("last_name");
            lastNameField.setAccessible(true);
            userNameField.set(user, username);
            firstNameField.set(user, firstName);
            lastNameField.set(user, lastName);

            Field chatIdField = chat.getClass().getDeclaredField("id");
            chatIdField.setAccessible(true);
            chatIdField.set(chat, chatId);

            Field messageTextField = message.getClass().getDeclaredField("text");
            messageTextField.setAccessible(true);
            Field messageChatField = message.getClass().getDeclaredField("chat");
            messageChatField.setAccessible(true);
            Field messageUserField = message.getClass().getDeclaredField("from");
            messageUserField.setAccessible(true);
            messageTextField.set(message, messageText);
            messageChatField.set(message, chat);
            messageUserField.set(message, user);

            Field updateMessageField = update.getClass().getDeclaredField("message");
            updateMessageField.setAccessible(true);
            updateMessageField.set(update, message);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return update;
    }

    private String generateNameIfEmpty(String name) {
        if (name.length() == 0) {
            return faker.name().username();
        }
        return name;
    }

    private Long generateIdIfEmpty(Long id) {
        if (id < 0) {
            Long idTemp = -1L;
            //id with <100 I leave for my needs
            while (idTemp < 100) {
                idTemp = faker.random().nextLong();
            }
            return idTemp;
        }
        return id;
    }

    private String generateMessageIfEmpty(String message) {
        if (message.length() == 0) {
            return faker.lordOfTheRings().character();
        }
        return message;
    }

}
