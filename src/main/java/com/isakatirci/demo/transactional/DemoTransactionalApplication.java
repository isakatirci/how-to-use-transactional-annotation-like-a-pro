package com.isakatirci.demo.transactional;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@SpringBootApplication
public class DemoTransactionalApplication {

    final private SpeakersRepository speakersRepository;
    final private SpeakerService speakerService;
    final private HistoryRepository historyRepository;
    final private StreamBridge streamBridge;


    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoTransactionalApplication.class, args);
        DemoTransactionalApplication u = context.getBean(DemoTransactionalApplication.class);
        u.extracted();
    }

    private void extracted() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 8000; i++) {
            final Likes likes = new Likes();
            likes.setTalkName("İSA");
            likes.setLikes(1);
            Thread thread = new Thread(() -> speakerService.addLikesToSpeaker(likes));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Total likes: " + speakersRepository.count());
    }

    @Bean
    public CommandLineRunner loadData() {
        return (args) -> {
            speakersRepository.findByTalkName("İSA").ifPresentOrElse((speaker) -> {
                speaker.setLikes(0);
                speakersRepository.save(speaker);
            }, () -> {
                Speaker speaker = new Speaker();
                speaker.setId(1l);
                speaker.setLikes(0);
                speaker.setTalkName("İSA");
                speakersRepository.save(speaker);

            });
        };
    }

}
