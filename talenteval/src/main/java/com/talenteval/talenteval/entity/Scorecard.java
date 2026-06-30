package com.talenteval.talenteval.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scorecards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scorecard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(nullable = false)
    private int communication;

    @Column(nullable = false)
    private int structure;

    @Column(nullable = false)
    private int content;

    @Column(nullable = false)
    private int confidence;

    @Column(columnDefinition = "TEXT")
    private String comments;
}
