package com.example.demo.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // ★ JPQLのSELECT NEW句で使うため、全引数コンストラクタが重要
public class TagVoteCountDto {
    private Long tagId;
    private String tagName;
    private long voteCount; // 票数
}