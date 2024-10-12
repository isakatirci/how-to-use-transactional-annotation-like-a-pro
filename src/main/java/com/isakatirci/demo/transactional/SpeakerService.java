package com.isakatirci.demo.transactional;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Repeatable;
import java.sql.SQLException;

@Component
@EnableRetry
public class SpeakerService {
    private final SpeakersRepository speakersRepository;
    private final HistoryRepository historyRepository;
    private final StreamBridge streamBridge;

    public SpeakerService(SpeakersRepository speakersRepository, HistoryRepository historyRepository, StreamBridge streamBridge) {
        this.speakersRepository = speakersRepository;
        this.historyRepository = historyRepository;
        this.streamBridge = streamBridge;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 15, backoff = @Backoff(delay = 1000))
    public void addLikesToSpeaker(Likes like) {
        if (like.getTalkName() != null) {
            speakersRepository.findByTalkName(like.getTalkName()).ifPresentOrElse(speaker -> {
                saveMessageToHistory(like, "RECEIVED");
                speaker.setLikes(speaker.getLikes() + like.getLikes());
                speakersRepository.save(speaker);
                System.out.printf("%s like added to %s%n", like.getLikes(), speaker.getFirstName() + " " + speaker.getLastName());
            }, () -> {
                System.out.printf("Speaker with talk %s not found", like.getTalkName());
                saveMessageToHistory(like, "ORPHANED");
            });
        } else {
            System.out.println("Error during adding like, no IDs given");
            saveMessageToHistory(like, "CORRUPTED");
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
            System.out.printf("Failed to save message to history. %1s", ex);
        }
    }
}
