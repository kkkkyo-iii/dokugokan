package com.example.demo.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.voting.Tag;
import com.example.demo.voting.TagRepository;
import com.example.demo.voting.TagVoteType;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;

    public DataInitializer(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    private void createTagIfNotFound(String name, String headline, TagVoteType type) {
        if (tagRepository.findByName(name).isEmpty()) {
            Tag tag = new Tag();
            tag.setName(name);
            tag.setCategoryHeadline(headline);
            tag.setTagType(type);
            tagRepository.save(tag);
            System.out.println("タグ: [" + name + "] (" + headline + ") を登録しました。");
        }
    }

    @Override
    public void run(String... args) throws Exception {
        
        System.out.println("タグ初期データの投入を開始します...");

        // --- 1. 気まずさの要因 (AWKWARD_REASON) ---
        String headline1_1 = "刺激・ショック";
        createTagIfNotFound("性的な描写", headline1_1, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("暴力・流血", headline1_1, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("グロテスク", headline1_1, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("ホラー・恐怖", headline1_1, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("下品な表現", headline1_1, TagVoteType.AWKWARD_REASON);

        String headline1_2 = "ずっしり・重め";
        createTagIfNotFound("モヤモヤが残る", headline1_2, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("気分が落ち込む", headline1_2, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("胸が痛む", headline1_2, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("考え込む", headline1_2, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("精神的に疲れる", headline1_2, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("シリアスな展開", headline1_2, TagVoteType.AWKWARD_REASON);

        String headline1_3 = "デリケートな話題";
        createTagIfNotFound("不倫・浮気", headline1_3, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("家族間の対立", headline1_3, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("差別・偏見", headline1_3, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("いじめ", headline1_3, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("命の重さ", headline1_3, TagVoteType.AWKWARD_REASON);
        createTagIfNotFound("宗教・政治", headline1_3, TagVoteType.AWKWARD_REASON);

        // --- 2. 読後感 (IMPRESSION) ---
        String headline2_1 = "スッキリ・元気";
        createTagIfNotFound("スカッとする", headline2_1, TagVoteType.IMPRESSION);
        createTagIfNotFound("元気が出る", headline2_1, TagVoteType.IMPRESSION);
        createTagIfNotFound("幸せな気分", headline2_1, TagVoteType.IMPRESSION);
        createTagIfNotFound("笑える", headline2_1, TagVoteType.IMPRESSION);
        createTagIfNotFound("爽やか", headline2_1, TagVoteType.IMPRESSION);
        createTagIfNotFound("安心する", headline2_1, TagVoteType.IMPRESSION);

        String headline2_2 = "心が動いた";
        createTagIfNotFound("心温まる", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("泣ける", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("感動", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("勇気をもらえる", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("やさしい気持ち", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("切ない", headline2_2, TagVoteType.IMPRESSION);
        createTagIfNotFound("余韻が残る", headline2_2, TagVoteType.IMPRESSION);

        String headline2_3 = "深い・考えさせられる";
        createTagIfNotFound("考えさせられる", headline2_3, TagVoteType.IMPRESSION);
        createTagIfNotFound("深い", headline2_3, TagVoteType.IMPRESSION);
        createTagIfNotFound("学びがある", headline2_3, TagVoteType.IMPRESSION);
        createTagIfNotFound("誰かに話したい", headline2_3, TagVoteType.IMPRESSION);
        createTagIfNotFound("新しい視点", headline2_3, TagVoteType.IMPRESSION);

        String headline2_4 = "センス・雰囲気";
        createTagIfNotFound("映像が美しい", headline2_4, TagVoteType.IMPRESSION);
        createTagIfNotFound("音楽がいい", headline2_4, TagVoteType.IMPRESSION);
        createTagIfNotFound("世界観が好き", headline2_4, TagVoteType.IMPRESSION);
        createTagIfNotFound("おしゃれ", headline2_4, TagVoteType.IMPRESSION);
        createTagIfNotFound("キャストがいい", headline2_4, TagVoteType.IMPRESSION);
        createTagIfNotFound("伏線がすごい", headline2_4, TagVoteType.IMPRESSION);

        System.out.println("タグ初期データの投入が完了しました。");
    }
}