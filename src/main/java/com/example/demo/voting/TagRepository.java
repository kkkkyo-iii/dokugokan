package com.example.demo.voting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * タグ名を元にタグを検索する (重複登録防止用)
     * @param name タグ名
     * @return タグエンティティ
     */
    Optional<Tag> findByName(String name);
    
 // 指定された TagVoteType (IMPRESSIONなど) に一致するタグだけを検索する
    List<Tag> findByTagType(TagVoteType tagType);
}