package com.example.fortunecookie.util;

import com.example.fortunecookie.entity.Fortune;
import com.example.fortunecookie.repository.FortuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/*
@Component
public class FortuneLoader implements CommandLineRunner {

    @Autowired
    private FortuneRepository fortuneRepository;

    @Override
    public void run(String... args) throws Exception {
        MessageReader messageReader = new MessageReader();
        List<String> messages = messageReader.readMessages("/Users/libohyun/dev/cdk-project/memo/src/main/resources/templates/messages.txt");

        messages.forEach(message -> {
            Fortune fortune = new Fortune(message);
            fortuneRepository.save(fortune);
        });

        System.out.println("Fortunes loaded into MongoDB.");
    }
}*/
