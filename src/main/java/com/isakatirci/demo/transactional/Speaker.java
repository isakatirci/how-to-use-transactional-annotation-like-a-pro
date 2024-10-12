package com.isakatirci.demo.transactional;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@Builder
@Table(name = "speaker")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Speaker {
    @Id
    private Long id;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "talkname")
    private String talkName;

    private int likes;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;
}
