package com.isakatirci.demo.transactional;

public class SpeakerMessageProcessor {
    private final SpeakerService speakerService;

    public SpeakerMessageProcessor(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    public void processOneMessage(Likes likes) {
        speakerService.addLikesToSpeaker(likes);
    }
}
