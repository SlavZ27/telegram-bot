package pro.sky.telegrambot.component;


public enum Command {
    START("/start"),
    TIME_ZONE("/timezone"),

    CALENDAR("/calendar"),
    ADMIN_GET_ALL_NOTIFICATION("/get_all_notification_admin"),
    ADMIN_DELETE_NOTIFICATION_BY_ID("/delete_notification_by_id_admin"),
    NOTIFICATION("/notification"),
    GET_ALL_NOTIFICATION("/get_all_notification"),
    CLEAR_ALL_NOTIFICATION("/clear_all_notification"),
    EMPTY_CALLBACK_DATA_FOR_BUTTON("...");


    private String title;

    Command(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Command fromString(String text) {
        for (Command command : Command.values()) {
            if (command.getTitle().equalsIgnoreCase(text)) {
                return command;
            }
        }
        return null;
    }

}
