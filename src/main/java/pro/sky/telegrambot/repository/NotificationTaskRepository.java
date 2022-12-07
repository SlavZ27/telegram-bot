package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Query(value = "select * from notification_task where is_done = false and local_date_time between :localDateTime1 and :localDateTime2", nativeQuery = true)
    List<NotificationTask> findAllByDoneIsFalseAndDateTimeIsBetween(LocalDateTime localDateTime1, LocalDateTime localDateTime2);

    @Query(value = "select * from notification_task where is_done = true and text_message=:textMessage", nativeQuery = true)
    NotificationTask getFirstByTextMessageAndAndDone(String textMessage);

    List<NotificationTask> getAllByChat_Id(Long chat_id);
}
