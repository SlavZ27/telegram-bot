package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.component.Command;
import pro.sky.telegrambot.component.TimeUnit;
import pro.sky.telegrambot.entity.Chat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {
    private static String LOCAL_TIME_ZONE;
    private final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    @Autowired
    private TelegramBotSenderService telegramBotSenderService;
    @Autowired
    private ChatService chatService;

    public CalendarService(@Value("${local.time.zone}") String ltz) {
        LOCAL_TIME_ZONE = ltz;
    }

    public void calendarStart(Update update, Command subCommand) {
        Long idChat = update.message().chat().id();
        logger.info("ChatId={}; Method calendarStart was start for start generate calendar for command = {}", idChat, subCommand.getTitle());
        logger.debug("ChatId={}; Method calendarStart going to start method sendYear", idChat);
        sendYear(update.message().chat().id(), subCommand);
    }

    public void processNext(Update update) {
        Long idChat = update.callbackQuery().from().id();
        logger.info("ChatId={}; Method processNext was start for continue generate calendar", idChat);
        String callbackQuery = update.callbackQuery().data();

        String[] callbackQueryMas = callbackQuery.split(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
        TimeUnit timeUnit = TimeUnit.valueOf(callbackQueryMas[callbackQueryMas.length - 2]);

        switch (timeUnit) {
            case YEAR:
                logProcessNextDetectedValidCommand(timeUnit.getTitle(), idChat);
                sendMonth(
                        idChat,
                        Command.fromString(callbackQueryMas[1]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case MONTH:
                logProcessNextDetectedValidCommand(timeUnit.getTitle(), idChat);
                sendDay(
                        idChat,
                        Command.fromString(callbackQueryMas[1]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case DAY:
                logProcessNextDetectedValidCommand(timeUnit.getTitle(), idChat);
                sendHour(
                        idChat,
                        Command.fromString(callbackQueryMas[1]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 5]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
            case HOUR:
                logProcessNextDetectedValidCommand(timeUnit.getTitle(), idChat);
                sendMinutes(idChat,
                        Command.fromString(callbackQueryMas[1]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 7]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 5]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 3]),
                        Integer.parseInt(callbackQueryMas[callbackQueryMas.length - 1])
                );
                break;
        }
    }

    private void sendMinutes(Long idChat, Command subCommand, int year, int month, int day, int hour) {
        logSendMessageForContinueGenerateCalendar("sendMinutes", idChat);
        List<String> tableMinutes = new ArrayList<>();
        for (int i = 0; i < 60; i = i + 5) {
            tableMinutes.add(String.valueOf(i));
        }
        telegramBotSenderService.sendButtons(idChat, "Select minutes",
                getRequestFinish(subCommand, year, month, day, hour),
                tableMinutes, tableMinutes, 3, 4);
    }

    private void sendHour(Long idChat, Command subCommand, int year, int month, int day) {
        logSendMessageForContinueGenerateCalendar("sendHour", idChat);

        List<String> tableHourData = new ArrayList<>();
        List<String> tableHour = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            tableHour.add(getTitleForButton(idChat, TimeUnit.HOUR, i));
            tableHourData.add(String.valueOf(i));
        }
        telegramBotSenderService.sendButtons(idChat, "Select hour",
                getRequestContinueForButton(subCommand, year, month, day),
                tableHour, tableHourData, 6, 4);
    }

    private void sendDay(Long idChat, Command subCommand, int year, int month) {
        logSendMessageForContinueGenerateCalendar("sendDay", idChat);
        List<String> tableDay = new ArrayList<>();
        List<String> tableDayData = new ArrayList<>();
        int lengthMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        for (int i = 1; i <= lengthMonth; i++) {
            tableDay.add(getTitleForButton(idChat, TimeUnit.DAY, i));
            tableDayData.add(String.valueOf(i));
        }
        int tableHeight = 6;
        if (lengthMonth < 31) {
            tableHeight = 5;
        }
        telegramBotSenderService.sendButtons(idChat, "Select day",
                getRequestContinueForButton(subCommand, year, month, -1),
                tableDay,
                tableDayData,
                6, tableHeight);
    }

    private void sendMonth(Long idChat, Command subCommand, int year) {
        logSendMessageForContinueGenerateCalendar("sendMonth", idChat);
        List<String> tableMonth = new ArrayList<>();
        List<String> tableMonthData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            tableMonth.add(getTitleForButton(idChat, TimeUnit.MONTH, i));
            tableMonthData.add(String.valueOf(i));
        }
        telegramBotSenderService.sendButtons(idChat, "Select month",
                getRequestContinueForButton(subCommand, year, -1, -1),
                tableMonth,
                tableMonthData,
                3, 4);
    }

    private void sendYear(Long idChat, Command subCommand) {
        logSendMessageForContinueGenerateCalendar("sendYear", idChat);
        List<String> tableYear = new ArrayList<>();
        List<String> tableYearData = new ArrayList<>();
        int yearNow = LocalDateTime.now().getYear();
        byte yearCount = 4;
        for (int i = yearNow; i < yearNow + yearCount; i++) {
            tableYear.add(getTitleForButton(idChat, TimeUnit.YEAR, i));
            tableYearData.add(String.valueOf(i));
        }
        telegramBotSenderService.sendButtons(idChat, "Select year",
                getRequestContinueForButton(subCommand, -1, -1, -1),
                tableYear,
                tableYearData,
                4, 1);
    }

    private void logProcessNextDetectedValidCommand(String timeUnitTitle, long idChat) {
        logger.info("ChatId={}; Method processNext was detected command: {}", idChat, timeUnitTitle);
    }

    private void logSendMessageForContinueGenerateCalendar(String method, long idChat) {
        logger.info("ChatId={}; Method {} was start for send message for continue generate calendar query",
                idChat, method);
    }

    private int getValueOfNowTimeUnitForTimeZoneOfChat(Long idChat, TimeUnit timeUnit) {
        Chat chat = chatService.findChat(idChat);
        int fadeTimeZoneInHour = 0;
        if (chat != null) {
            fadeTimeZoneInHour = chat.getTimeZone();
        }
        LocalDateTime localDateTime = LocalDateTime.now().
                plusHours(fadeTimeZoneInHour).
                minusHours(CalendarService.getLocalTimeZone());
        switch (timeUnit) {
            case HOUR:
                return localDateTime.getHour();
            case DAY:
                return localDateTime.getDayOfMonth();
            case MONTH:
                return localDateTime.getMonthValue();
            case YEAR:
                return localDateTime.getYear();
            default:
                return 0;
        }
    }

    private String getTitleForButton(Long idChat, TimeUnit timeUnit, int number) {
        if (number == getValueOfNowTimeUnitForTimeZoneOfChat(idChat, timeUnit)) {
            return TimeUnit.NOW.getTitle();
        } else if (number == getValueOfNowTimeUnitForTimeZoneOfChat(idChat, timeUnit) + 1) {
            return TimeUnit.NEXT.getTitle();
        } else {
            return String.valueOf(number);
        }
    }

    private String getRequestFinish(Command subCommand, int year, int month, int day, int hour) {
        StringBuilder sb = new StringBuilder();
        sb.append(subCommand.getTitle());
        sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
        sb.append(TimeUnit.YEAR);
        if (year > 0) {
            sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
            sb.append(year);
            sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
            sb.append(TimeUnit.MONTH);
            if (month > 0) {
                sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                sb.append(month);
                sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                sb.append(TimeUnit.DAY);
                if (day > 0) {
                    sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                    sb.append(day);
                    sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                    sb.append(TimeUnit.HOUR);
                    if (hour > 0) {
                        sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                        sb.append(hour);
                        sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                        sb.append(TimeUnit.MINUTE);
                    }
                }
            }
        }
        return sb.toString();
    }

    private String getRequestContinueForButton(Command subCommand, int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        sb.append(Command.CALENDAR.getTitle());
        sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
        sb.append(subCommand.getTitle());
        sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
        sb.append(TimeUnit.YEAR);
        if (year > 0) {
            sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
            sb.append(year);
            sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
            sb.append(TimeUnit.MONTH);
            if (month > 0) {
                sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                sb.append(month);
                sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                sb.append(TimeUnit.DAY);
                if (day > 0) {
                    sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                    sb.append(day);
                    sb.append(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
                    sb.append(TimeUnit.HOUR);
                }
            }
        }
        return sb.toString();
    }

    public static int getLocalTimeZone() {
        return Integer.parseInt(LOCAL_TIME_ZONE);
    }

}
