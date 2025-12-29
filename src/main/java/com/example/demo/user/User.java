package com.example.demo.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
	@NotBlank(message = "ユーザー名は必須です")
	@Size(min = 4, max = 20, message = "4文字以上20文字以内で入力してください")
	private String username;

    // ハッシュ化されたパスワード
	@Column(nullable = false)
    @NotBlank(message = "パスワードは必須です")
    // maxはハッシュ化後(約60文字)も許容できるように100文字以上にしておく
    @Size(min = 8, max = 120, message = "パスワードは8文字以上で入力してください")
    private String password;

    // 権限 (例: "ROLE_USER", "ROLE_ADMIN")
    @Column(nullable = false)
    private String role;
}