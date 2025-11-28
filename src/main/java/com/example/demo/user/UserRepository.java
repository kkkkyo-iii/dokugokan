package com.example.demo.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ユーザー名（username）を元にユーザー情報を検索する.
     * Spring Securityがログイン認証を行う際に、このメソッドを内部的に利用する.
     * @param username 検索するユーザー名
     * @return ユーザーエンティティ（見つからない場合は空のOptional）
     */
    Optional<User> findByUsername(String username);

}