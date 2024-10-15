package com.isakatirci.demo.transactional;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Repeatable;
import java.sql.SQLException;

@Service
@EnableRetry
//@EnableScheduling
//@EnableAsync
@RequiredArgsConstructor
public class SpeakerService {
    private final SpeakersRepository speakersRepository;
    private final HistoryRepository historyRepository;
    private final StreamBridge streamBridge;
    private final HistoryService historyService;


    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 1)
    //@Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 15, backoff = @Backoff(delay = 1000))
    //@Scheduled(fixedDelay = 1000)
    //@Async
    public void addLikesToSpeaker(Likes like) {
        if (like.getTalkName() != null) {
            speakersRepository.findByTalkName(like.getTalkName()).ifPresentOrElse(speaker -> {
                historyService.saveMessageToHistory(like, "RECEIVED");
                speaker.setLikes(speaker.getLikes() + like.getLikes());
                speakersRepository.save(speaker);
                System.out.printf("%s like added to %s%n", like.getLikes(), speaker.getFirstName() + " " + speaker.getLastName());
            }, () -> {
                System.err.printf("Speaker with talk %s not found", like.getTalkName());
                historyService.saveMessageToHistory(like, "ORPHANED");
            });
        } else {
            System.err.println("Error during adding like, no IDs given");
            historyService.saveMessageToHistory(like, "CORRUPTED");
        }
    }

    public void createTaskToAddLikes(Likes likes) {
        streamBridge.send("likesProducer-out-0", likes);
    }

    private void saveMessageToHistory(Likes like, String status) {
        try {
            historyRepository.save(History.builder()
                    .talkName(like.getTalkName())
                    .likes(like.getLikes())
                    .status(status)
                    .build());
        } catch (RuntimeException ex) {
            System.err.printf("Failed to save message to history. %1s", ex);
        }
    }
}
