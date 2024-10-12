package com.isakatirci.demo.transactional;

import org.springframework.stereotype.Component;

@Component
public class SpeakerService {
    private final SpeakersRepository speakersRepository;
    private final HistoryRepository historyRepository;
    private final StreamBridge streamBridge;

    public SpeakerService(SpeakersRepository speakersRepository, HistoryRepository historyRepository, StreamBridge streamBridge) {
        this.speakersRepository = speakersRepository;
        this.historyRepository = historyRepository;
        this.streamBridge = streamBridge;
    }

    public void addLikesToSpeaker(Likes likes) {
        if (likes.getTalkName() != null) {
            speakersRepository.findByTalkName(likes.getTalkName()).ifPresentOrElse(speaker -> {
                saveMessageToHistory(likes, "RECEIVED");
                speaker.setLikes(speaker.getLikes() + likes.getLikes());
                speakersRepository.save(speaker);
                System.out.printf("%1s likes added to %1s%n", likes.getLikes(), speaker.getFirstName() + " " + speaker.getLastName());
            }, () -> {
                System.out.printf("Speaker with talk %1s not found", likes.getTalkName());
                saveMessageToHistory(likes, "ORPHANED");
            });
        } else {
            System.out.println("Error during adding likes, no IDs given");
            saveMessageToHistory(likes, "CORRUPTED");
        }
    }

    public void createTaskToAddLikes(Likes likes) {
        streamBridge.send("likesProducer-out-0", likes);
    }

    private void saveMessageToHistory(Likes likes, String status) {
        try {
           /* historyRepository.save(History.builder()
                    .talkName(likes.getTalkName())
                    .likes(likes.getLikes())
                    .status(status)
                    .build());*/
        } catch (RuntimeException ex) {
            System.out.printf("Failed to save message to history. %1s", ex);
        }
    }
}
