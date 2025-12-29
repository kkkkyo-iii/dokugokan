package com.example.demo.voting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
	// 人気タグランキング用
    List<Tag> findTop10ByOrderByTotalVoteCountDesc();

    // カウンター操作（排他制御）
    @Modifying
    @Query("UPDATE Tag t SET t.totalVoteCount = t.totalVoteCount + 1 WHERE t.id = :id")
    void incrementVoteCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Tag t SET t.totalVoteCount = t.totalVoteCount - 1 WHERE t.id = :id AND t.totalVoteCount > 0")
    void decrementVoteCount(@Param("id") Long id);
	
    /**
     * タグ名を元にタグを検索する (重複登録防止用)
     * @param name タグ名
     * @return タグエンティティ
     */
    Optional<Tag> findByName(String name);
    
 // 指定された TagVoteType (IMPRESSIONなど) に一致するタグだけを検索する
    List<Tag> findByTagType(TagVoteType tagType);
}