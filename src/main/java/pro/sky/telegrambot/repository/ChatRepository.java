package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Chat getChatById(Long id);
}
