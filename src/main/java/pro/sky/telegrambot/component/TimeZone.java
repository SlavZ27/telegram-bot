package pro.sky.telegrambot.component;


import java.util.List;

public enum TimeZone {
    TM11(-11, "-11"),
    TM10(-10, "-10"),
    TM9(-9, "-9"),
    TM8(-8, "-8"),
    TM7(-7, "-7"),
    TM6(-6, "-6"),
    TM5(-5, "-5"),
    TM4(-4, "-4"),
    TM3(-3, "-3"),
    TM2(-2, "-2"),
    TM1(-1, "-1"),
    T0(0, "0"),
    TP1(1, "+1"),
    TP2(2, "+2"),
    TP3(3, "+3"),
    TP4(4, "+4"),
    TP5(5, "+5"),
    TP6(6, "+6"),
    TP7(7, "+7"),
    TP8(8, "+8"),
    TP9(9, "+9"),
    TP10(10, "+10"),
    TP11(11, "+11"),
    TP12(12, "+12");

    private int hour;
    private String title;

    TimeZone(int hour, String title) {
        this.hour = hour;
        this.title = title;
    }

    public int getHour() {
        return hour;
    }

    public String getTitle() {
        return title;
    }

    public static List<Integer> getListHour() {
        return List.of(
                TM11.getHour(),
                TM10.getHour(),
                TM9.getHour(),
                TM8.getHour(),
                TM7.getHour(),
                TM6.getHour(),
                TM5.getHour(),
                TM4.getHour(),
                TM3.getHour(),
                TM2.getHour(),
                TM1.getHour(),
                T0.getHour(),
                TP1.getHour(),
                TP2.getHour(),
                TP3.getHour(),
                TP4.getHour(),
                TP5.getHour(),
                TP6.getHour(),
                TP7.getHour(),
                TP8.getHour(),
                TP9.getHour(),
                TP10.getHour(),
                TP11.getHour(),
                TP12.getHour());
    }

    public static List<String> getListTitle() {
        return List.of(
                TM11.getTitle(),
                TM10.getTitle(),
                TM9.getTitle(),
                TM8.getTitle(),
                TM7.getTitle(),
                TM6.getTitle(),
                TM5.getTitle(),
                TM4.getTitle(),
                TM3.getTitle(),
                TM2.getTitle(),
                TM1.getTitle(),
                T0.getTitle(),
                TP1.getTitle(),
                TP2.getTitle(),
                TP3.getTitle(),
                TP4.getTitle(),
                TP5.getTitle(),
                TP6.getTitle(),
                TP7.getTitle(),
                TP8.getTitle(),
                TP9.getTitle(),
                TP10.getTitle(),
                TP11.getTitle(),
                TP12.getTitle());
    }

}
