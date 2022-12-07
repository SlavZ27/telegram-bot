package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimeZoneService {

    public final static String COMMAND_TIME_ZONE = "/timezone";
    public final static int VARIABLE_LOCAL_TIME_ZONE = 5;

    public List<String> getTimeZoneList() {
        return new ArrayList<>(List.of(
                "-11",
                "-10",
                "-9",
                "-8",
                "-7",
                "-6",
                "-5",
                "-4",
                "-3",
                "-2",
                "-1",
                "0",
                "+1",
                "+2",
                "+3",
                "+4",
                "+5",
                "+6",
                "+7",
                "+8",
                "+9",
                "+10",
                "+11",
                "+12"));
    }

}
