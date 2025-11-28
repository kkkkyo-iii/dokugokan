package com.example.demo.voting.dto;

import java.util.List;

import lombok.Data;

@Data
public class VoteResultDto {
    // A. 「気まずさ」表示セクション
    private int awkwardPercent = 0; // 「気まずい」と投票した人の割合 (0-100)
    private int notAwkwardPercent = 0; // 「気まずくない」と投票した人の割合 (0-100)
    private long totalAwkwardVotes = 0; // 気まずさ投票の総数
    private List<TagVoteCountDto> awkwardReasonTags; // 気まずい理由タグ (Top 3 + それ以降)

    // B. 「読後感」タグ表示セクション
    private List<TagVoteCountDto> impressionTags; // 読後感タグ (Top 5 + それ以降)
}