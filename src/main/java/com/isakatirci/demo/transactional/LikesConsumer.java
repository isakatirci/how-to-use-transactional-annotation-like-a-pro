package com.isakatirci.demo.transactional;


public class LikesConsumer {
    private final SpeakerMessageProcessor messageProcessor;

    public LikesConsumer(SpeakerMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }


    public void accept(Likes likes) {
        System.out.println("Message received: " + likes);
        messageProcessor.processOneMessage(likes);
    }
}
