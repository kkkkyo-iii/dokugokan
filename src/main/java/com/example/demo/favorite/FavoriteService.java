package com.example.demo.favorite;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.movie.Movie;
import com.example.demo.movie.MovieInformationRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieInformationRepository movieInformationRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, 
                           UserRepository userRepository, 
                           MovieInformationRepository movieInformationRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieInformationRepository = movieInformationRepository;
    }

    /**
     * お気に入りに登録する
     * @param movieId DBの映画ID
     * @param username ログイン中のユーザー名
     * @return 処理対象のMovieオブジェクト (リダイレクト用)
     */
    @Transactional
    public Movie addFavorite(Long movieId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieInformationRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        // 既にお気に入りでないか確認してから保存
        if (favoriteRepository.findByUserAndMovie(user, movie).isEmpty()) {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setMovie(movie);
            favoriteRepository.save(favorite);
        }
        
        return movie; // リダイレクト先URLの組み立てに使うためMovieを返す
    }

    /**
     * お気に入りを解除する
     * @param movieId DBの映画ID
     * @param username ログイン中のユーザー名
     * @return 処理対象のMovieオブジェクト (リダイレクト用)
     */
    @Transactional
    public Movie removeFavorite(Long movieId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieInformationRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        // 存在すれば削除
        favoriteRepository.findByUserAndMovie(user, movie).ifPresent(favorite -> {
            favoriteRepository.delete(favorite);
        });
        
        return movie; // リダイレクト先URLの組み立てに使うためMovieを返す
    }

    /**
     * 映画がお気に入り登録されているか確認する（MovieService用）
     * @param user ユーザー
     * @param movie 映画
     * @return お気に入りならtrue
     */
    public boolean isFavorited(User user, Movie movie) {
        if (user == null || movie == null) {
            return false;
        }
        return favoriteRepository.findByUserAndMovie(user, movie).isPresent();
    }
    
    /**
     *  ユーザーのお気に入り映画リストを取得する (ページング対応)
     * @param user 対象ユーザー
     * @param page ページ番号 (1始まり) ← 引数を追加
     * @return 映画のページ情報
     */
 
    public Page<Movie> getFavoriteMovies(User user, int page) {
        
        // 1ページあたりの表示件数
        int pageSize = 10;
        
        // ページング設定 (ページ番号は0始まりに変換、IDの降順で並べ替え)
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());

        Page<Favorite> favoritePage = favoriteRepository.findByUser(user, pageable);

        // FavoriteのPageをMovieのPageに変換して返す
        return favoritePage.map(Favorite::getMovie);
    }
}