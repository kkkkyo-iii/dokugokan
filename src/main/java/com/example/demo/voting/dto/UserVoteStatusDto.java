package com.example.demo.voting.dto;

import java.util.Collections;
import java.util.Set;

import lombok.Data;

/**
 * ログインユーザーの、ある映画に対する現在の投票状態を保持するDTO
 */
@Data
public class UserVoteStatusDto {

    // ユーザーが「気まずい(true)」「気まずくない(false)」のどちらに投票したか
    // まだ投票していない場合は null
    private Boolean awkwardVote;

    // ユーザーが「気まずい理由」として投票したタグのIDのセット
    private Set<Long> awkwardReasonTagIds = Collections.emptySet();

    // ユーザーが「読後感」として投票したタグのIDのセット
    private Set<Long> impressionTagIds = Collections.emptySet();

    // いずれかの投票を既に行っているか
    private boolean hasVoted = false;
}