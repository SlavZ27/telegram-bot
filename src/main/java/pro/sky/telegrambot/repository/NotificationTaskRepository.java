package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Query(value = "select * from notification_task where is_deleted = false and is_done = false and local_date_time between :localDateTime1 and :localDateTime2", nativeQuery = true)
    List<NotificationTask> findAllByDoneIsFalseAndDateTimeIsBetween(LocalDateTime localDateTime1, LocalDateTime localDateTime2);

    @Query(value = "select * from notification_task where is_deleted = false and is_done = true and id_chat=:idChat and text_message=:textMessage", nativeQuery = true)
    NotificationTask getFirstByTextMessageAndIdChatAndDone(Long idChat, String textMessage);

    @Query(value = "select * from notification_task where is_deleted = false and is_done = false and id_chat=:chat_id", nativeQuery = true)
    List<NotificationTask> getAllByChat_IdUndeletedAndUndone(Long chat_id);

    @Query(value = "select * from notification_task where is_deleted = false and id_chat=:chat_id", nativeQuery = true)
    List<NotificationTask> getAllByChat_IdUndeleted(Long chat_id);

    @Query(value = "select * from notification_task order by id", nativeQuery = true)
    List<NotificationTask> getAll();

    NotificationTask getNotificationTaskById(Long idNotificationTask);
}
