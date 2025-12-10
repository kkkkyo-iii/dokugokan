package com.example.demo.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // テーブル名を "users" に指定
@Data
@NoArgsConstructor
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    // ユーザー名 (ログインID)
    @Column(nullable = false, unique = true)
    private String username;

    // ハッシュ化されたパスワード
    @Column(nullable = false)
    private String password;

    // 権限 (例: "ROLE_USER", "ROLE_ADMIN")
    @Column(nullable = false)
    private String role;
}