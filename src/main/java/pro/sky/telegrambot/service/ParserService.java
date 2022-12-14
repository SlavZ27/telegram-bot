package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    private final Logger logger = LoggerFactory.getLogger(ParserService.class);

    public String parseWord(String s, int indexWord) {
        logger.debug("Method parseWord was start for parse from string = {} word # = {}", s, indexWord);

        if (!s.contains(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL)) {
            logger.debug("Method parseWord don't found REQUEST_SPLIT_SYMBOL = {} and return", TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);
            return s;
        }
        String[] sMas = s.split(TelegramBotSenderService.REQUEST_SPLIT_SYMBOL);

        if (indexWord >= sMas.length) {
            logger.debug("Method parseWord detect index of word bigger of sum words in string and return empty string");
            return "";
        }
        logger.debug("Method parseWord return {}", sMas[indexWord]);
        return sMas[indexWord];
    }
}
