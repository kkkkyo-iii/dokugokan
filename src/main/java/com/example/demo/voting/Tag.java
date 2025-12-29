package com.example.demo.voting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Data 
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_id_seq")
    @SequenceGenerator(name = "tags_id_seq", sequenceName = "tags_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    
    // どの見出しに属するか
    @Column(name = "category_headline", nullable = false)
    private String categoryHeadline;

    // どちらの投票タイプか
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false)
    private TagVoteType tagType;

    // 総得票数 (初期値0)
    // カウンターキャッシュ用。毎回集計せずここを読むだけで済むようにする。
    @Column(nullable = false)
    private Long totalVoteCount = 0L;
}