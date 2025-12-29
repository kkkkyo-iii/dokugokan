package com.example.demo.voting;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;
import com.example.demo.voting.dto.TagVoteCountDto;

@DataJpaTest // JPAコンポーネントのみをロードして高速にDBテストを行う
class MovieTagVoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // テスト用データ挿入ヘルパー

    @Autowired
    private MovieTagVoteRepository repository;

    @Test
    @DisplayName("タグごとの投票数が正しく集計され、降順で取得できること")
    void findTagVoteCountsByMovie_Success() {
        // --- Arrange (テストデータ準備) ---
        // 1. ユーザー作成
        User user1 = new User(); user1.setUsername("user1"); user1.setPassword("password"); user1.setRole("USER");
        User user2 = new User(); user2.setUsername("user2"); user2.setPassword("password"); user2.setRole("USER");
        entityManager.persist(user1);
        entityManager.persist(user2);

        // 2. 映画作成
        Movie movie = new Movie();
        movie.setTmdbId(100);
        movie.setTitle("Test Movie");
        entityManager.persist(movie);

        // 3. タグ作成
        Tag tagA = new Tag(); tagA.setName("TagA"); tagA.setTagType(TagVoteType.IMPRESSION); tagA.setCategoryHeadline("H1");
        Tag tagB = new Tag(); tagB.setName("TagB"); tagB.setTagType(TagVoteType.IMPRESSION); tagB.setCategoryHeadline("H1");
        entityManager.persist(tagA);
        entityManager.persist(tagB);

        // 4. 投票データ作成 (TagAに2票, TagBに1票)
        createVote(user1, movie, tagA);
        createVote(user2, movie, tagA);
        createVote(user1, movie, tagB);

        entityManager.flush(); // DBに反映

        // --- Act (実行) ---
        List<TagVoteCountDto> results = repository.findTagVoteCountsByMovie(movie.getId(), TagVoteType.IMPRESSION);

        // --- Assert (検証) ---
        assertThat(results).hasSize(2);
        
        // 1位は TagA (2票)
        assertThat(results.get(0).getTagName()).isEqualTo("TagA");
        assertThat(results.get(0).getVoteCount()).isEqualTo(2L);

        // 2位は TagB (1票)
        assertThat(results.get(1).getTagName()).isEqualTo("TagB");
        assertThat(results.get(1).getVoteCount()).isEqualTo(1L);
    }

    private void createVote(User user, Movie movie, Tag tag) {
        MovieTagVote vote = new MovieTagVote();
        vote.setUser(user);
        vote.setMovie(movie);
        vote.setTag(tag);
        vote.setVoteType(TagVoteType.IMPRESSION);
        entityManager.persist(vote);
    }
}