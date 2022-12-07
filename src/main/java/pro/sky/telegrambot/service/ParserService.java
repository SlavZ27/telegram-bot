package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    private final Logger logger = LoggerFactory.getLogger(ParserService.class);

    public String parseWord(String s, int indexWord) {
        if (!s.contains(" ")) {
            return s;
        }
        String[] sMas = s.split(" ");

        if (indexWord >= sMas.length) {
            return "";
        }
        return sMas[indexWord];
    }

}
