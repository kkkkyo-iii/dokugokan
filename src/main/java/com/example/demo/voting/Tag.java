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
    
 // ★ 3. どの見出しに属するか (例: "感覚への刺激", "気分（ポジティブ系）")
    @Column(name = "category_headline", nullable = false)
    private String categoryHeadline;

    // ★ 4. どちらの投票タイプか (AWKWARD_REASON または IMPRESSION)
    @Enumerated(EnumType.STRING) // Enumの値を文字列としてDBに保存
    @Column(name = "tag_type", nullable = false)
    private TagVoteType tagType;
}