package com.isakatirci.demo.transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpeakersRepository extends JpaRepository<Speaker, Long> {
    Optional<Speaker> findByTalkName(String talkName);


}
