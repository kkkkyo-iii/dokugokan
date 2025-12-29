package com.example.demo.favorite;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.movie.Movie;
import com.example.demo.movie.MovieInformationRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MovieInformationRepository movieRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    @DisplayName("まだお気に入り登録されていない場合、正常に保存されること")
    void addFavorite_Success() {
        // Arrange
        Long movieId = 1L;
        String username = "testuser";
        
        User mockUser = new User();
        mockUser.setUsername(username);
        
        Movie mockMovie = new Movie();
        mockMovie.setId(movieId);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(mockMovie));
        // まだ登録がない状態をシミュレート
        when(favoriteRepository.findByUserAndMovie(mockUser, mockMovie)).thenReturn(Optional.empty());

        // Act
        favoriteService.addFavorite(movieId, username);

        // Assert
        // saveメソッドが1回呼ばれたことを確認
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("既にお気に入り登録済みの場合、保存処理がスキップされること")
    void addFavorite_SkipIfExists() {
        // Arrange
        Long movieId = 1L;
        String username = "testuser";
        User mockUser = new User();
        Movie mockMovie = new Movie();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(mockMovie));
        // 既に登録がある状態をシミュレート
        when(favoriteRepository.findByUserAndMovie(mockUser, mockMovie)).thenReturn(Optional.of(new Favorite()));

        // Act
        favoriteService.addFavorite(movieId, username);

        // Assert
        // saveメソッドが呼ばれていない（0回）ことを確認
        verify(favoriteRepository, times(0)).save(any(Favorite.class));
    }
}