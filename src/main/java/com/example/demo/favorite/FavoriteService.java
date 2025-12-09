package com.example.demo.favorite;

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
     *  ユーザーのお気に入り映画リストを取得する 
     * @param user 対象ユーザー
     * @return 映画のリスト
     */
    public java.util.List<Movie> getFavoriteMovies(User user) {
        // 1. リポジトリから Favorite エンティティのリストを取得
        java.util.List<Favorite> favorites = favoriteRepository.findByUserOrderByIdDesc(user);

        // 2. Favorite のリストを stream で回して、中の Movie だけを取り出してリストにする
        return favorites.stream()
                .map(Favorite::getMovie) // Favorite から Movie を取り出す
                .collect(java.util.stream.Collectors.toList());
    }
}