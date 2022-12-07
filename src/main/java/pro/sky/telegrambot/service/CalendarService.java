package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.Chat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {
    public final static String COMMAND_CALENDAR = "/calendar";
    public final static String VARIABLE_YEAR = "year";
    public final static String VARIABLE_MONTH = "month";
    public final static String VARIABLE_DAY = "day";
    public final static String VARIABLE_HOUR = "hour";
    public final static String VARIABLE_MINUTES = "minutes";
    public final static String VARIABLE_NEXT = "next";
    public final static String VARIABLE_NOW = "now";

    private final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    @Autowired
    private TelegramBotSenderService telegramBotSenderService;
    @Autowired
    private ChatService chatService;

    public void calendarStart(Update update) {
        sendYear(update.message().chat().id());
    }

    public void processNext(Update update) {
        Long idChat = update.callbackQuery().from().id();
        String callbackQuery = update.callbackQuery().data();

        String[] callbackQueryMas = callbackQuery.split(" ");
        String command = callbackQueryMas[callbackQueryMas.length - 2];

        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }

        switch (command) {
            case VARIABLE_YEAR:
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NOW)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getYear());
                }
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NEXT)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getYear() + 1);
                }
                sendMonth(
                        idChat,
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case VARIABLE_MONTH:
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NOW)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getMonthValue());
                }
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NEXT)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getMonthValue() + 1);
                }
                sendDay(
                        idChat,
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case VARIABLE_DAY:
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NOW)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getDayOfMonth());
                }
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NEXT)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getDayOfMonth() + 1);
                }
                sendHour(
                        idChat,
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 5]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case VARIABLE_HOUR:
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NOW)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getHour());
                }
                if (callbackQueryMas[callbackQueryMas.length - 1].equals(VARIABLE_NEXT)) {
                    callbackQueryMas[callbackQueryMas.length - 1] =
                            String.valueOf(LocalDateTime.now().
                                    plusHours(fadeTimeZoneInHour).
                                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getHour() + 1);
                }
                sendMinutes(idChat,
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 7]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 5]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
        }
    }

    private void sendMinutes(Long idChat, int year, int month, int day, int hour) {
        List<String> tableHour = new ArrayList<>();
        for (int i = 0; i < 60; i = i + 5) {
            tableHour.add(String.valueOf(i));
        }

        telegramBotSenderService.sendButtons(idChat, "Select minutes",
                NotificationService.COMMAND_NOTIFICATION + " "
                        + VARIABLE_YEAR + " " + year + " "
                        + VARIABLE_MONTH + " " + month + " "
                        + VARIABLE_DAY + " " + day + " "
                        + VARIABLE_HOUR + " " + hour + " "
                        + VARIABLE_MINUTES,
                tableHour, 3, 4);
    }

    private void sendHour(Long idChat, int year, int month, int day) {
        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }
        List<String> tableHour = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getHour()) {
                tableHour.add(VARIABLE_NOW);
            } else if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getHour() + 1) {

                tableHour.add(VARIABLE_NEXT);
            } else {
                tableHour.add(String.valueOf(i));
            }
        }

        telegramBotSenderService.sendButtons(idChat, "Select hour",
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + VARIABLE_YEAR + " " + year + " "
                        + VARIABLE_MONTH + " " + month + " "
                        + VARIABLE_DAY + " " + day + " "
                        + VARIABLE_HOUR,
                tableHour, 6, 4);
    }

    private void sendDay(Long idChat, int year, int month) {
        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }
        List<String> tableDay = new ArrayList<>();
        int lengthMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        for (int i = 1; i <= lengthMonth; i++) {
            if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getDayOfMonth()) {
                tableDay.add(VARIABLE_NOW);
            } else if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getDayOfMonth() + 1) {

                tableDay.add(VARIABLE_NEXT);
            } else {
                tableDay.add(String.valueOf(i));
            }
        }
        int tableHeight = 6;
        if (lengthMonth < 31) {
            tableHeight = 5;
        }
        telegramBotSenderService.sendButtons(idChat, "Select day",
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + VARIABLE_YEAR + " " + year + " "
                        + VARIABLE_MONTH + " " + month + " "
                        + VARIABLE_DAY,
                tableDay, 6, tableHeight);
    }

    private void sendMonth(Long idChat, int year) {
        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }
        List<String> tableMonth = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getMonthValue()) {
                tableMonth.add(VARIABLE_NOW);
            }
            if (i == LocalDateTime.now().
                    plusHours(fadeTimeZoneInHour).
                    minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getMonthValue() + 1) {
                tableMonth.add(VARIABLE_NEXT);
            }
            tableMonth.add(String.valueOf(i));
        }
        telegramBotSenderService.sendButtons(idChat, "Select month",
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " "
                        + VARIABLE_YEAR + " " + year + " "
                        + VARIABLE_MONTH,
                tableMonth, 3, 4);
    }

    private void sendYear(Long idChat) {
        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }
        int nowYear = LocalDateTime.now().
                plusHours(fadeTimeZoneInHour).
                minusHours(TimeZoneService.VARIABLE_LOCAL_TIME_ZONE).getYear();
        telegramBotSenderService.sendButtons(idChat, "Select year",
                CalendarService.COMMAND_CALENDAR + " " +
                        NotificationService.COMMAND_NOTIFICATION + " " + VARIABLE_YEAR,
                new ArrayList<>(List.of(
                        VARIABLE_NOW,
                        VARIABLE_NEXT,
                        String.valueOf(nowYear + 2),
                        String.valueOf(nowYear + 3))), 4, 1);
    }
}
