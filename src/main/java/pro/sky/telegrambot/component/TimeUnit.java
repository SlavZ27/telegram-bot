package pro.sky.telegrambot.component;

import org.springframework.stereotype.Component;

public enum TimeUnit
{
    YEAR("year"),
    MONTH("month"),
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    NEXT("next"),
    NOW("now");

    private String title;

    TimeUnit(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
