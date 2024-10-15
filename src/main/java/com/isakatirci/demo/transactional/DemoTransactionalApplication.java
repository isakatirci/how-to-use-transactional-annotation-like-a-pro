package com.isakatirci.demo.transactional;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AllArgsConstructor
@SpringBootApplication
@EnableAsync
//@EnableScheduling
public class DemoTransactionalApplication {

    private final SpeakersRepository speakersRepository;
    private final SpeakerService speakerService;
    private final HistoryRepository historyRepository;
    private final StreamBridge streamBridge;
    private final static AtomicInteger counter = new AtomicInteger(1); // a global counter


    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoTransactionalApplication.class, args);
        DemoTransactionalApplication u = context.getBean(DemoTransactionalApplication.class);
        u.extracted();
    }

    private static void incrementCounter() {
        System.out.println("incrementCounter" + Thread.currentThread().getName() + ": " + counter.getAndIncrement());
    }

    //https://www.baeldung.com/spring-scheduled-tasks
    public void extracted() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final Likes likes = new Likes();
            likes.setTalkName("İSA");
            likes.setLikes(1);
            Thread thread = new Thread(() -> {
                try {
                    speakerService.addLikesToSpeaker(likes);
                } catch (Exception e) {
                    incrementCounter();
                    System.out.println("class name:" + e.getClass().getName() + " HATA!!!" + e.getMessage());
                }

            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        speakersRepository.findByTalkName("İSA").ifPresentOrElse((speaker) -> {
            System.out.println("Speaker Total likes: " + speaker.getLikes());
        }, () -> {


        });
    }

    @Bean
    public CommandLineRunner loadData() {
        return (args) -> {
            historyRepository.deleteAll();
            speakersRepository.findByTalkName("İSA").ifPresentOrElse((speaker) -> {
                speaker.setLikes(0);
                speakersRepository.save(speaker);
            }, () -> {
                Speaker speaker = new Speaker();
                speaker.setId(1l);
                speaker.setLikes(0);
                speaker.setFirstName("İSA");
                speaker.setLastName("KATIRCI");
                speaker.setLikes(0);
                speaker.setTalkName("İSA");
                speakersRepository.save(speaker);

            });
        };
    }

}
