package com.isakatirci.demo.transactional;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Table(name = "history")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "talkname")
    private String talkName;

    private int likes;

    private String status;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

}
