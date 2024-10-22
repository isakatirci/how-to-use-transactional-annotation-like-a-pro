package com.isakatirci.demo.transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@EnableRetry
//@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class SpeakerService {
    private final SpeakersRepository speakersRepository;
    private final HistoryRepository historyRepository;
    private final StreamBridge streamBridge;
    private final HistoryService historyService;


    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 60)
    @Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000), recover = "addLikesToSpeakerRecover")
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

    @Recover
    public void addLikesToSpeakerRecover(Exception ex, Likes likes) throws SQLException {
        if (likes.getTalkName() != null) {
            speakersRepository.findByTalkName(likes.getTalkName()).ifPresentOrElse(speaker -> {
                System.out.printf("Adding {} likes to {}", likes.getLikes(), speaker.getFirstName() + " " + speaker.getLastName());
                speaker.setLikes(speaker.getLikes() + likes.getLikes());
            }, () -> {
                System.out.printf("Speaker with talk {} not found", likes.getTalkName());
                saveMessageToHistory(likes, "ORPHANED");
            });
        } else {
            System.out.printf("Error during adding likes, no IDs given");
            saveMessageToHistory(likes, "CORRUPTED");
            throw new SQLException();
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

    public static void main(String[] args) {

    }
    public static int main(String[] args, int a) {
        return 0;
    }
}
