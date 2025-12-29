package com.example.demo.voting;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
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

@ExtendWith(MockitoExtension.class) // Mockitoを有効化
class VotingServiceTest {

    @Mock // DBアクセス等の依存クラスをモック（偽物）にする
    private MovieInformationRepository movieRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AwkwardnessVoteRepository awkwardnessVoteRepository;
    @Mock
    private MovieTagVoteRepository movieTagVoteRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks // モックを注入してテスト対象のインスタンスを作成
    private VotingService votingService;

    @Test
    @DisplayName("公開済みの映画に対して、正常に投票処理が完了すること")
    void processVotes_Success() {
        // --- 準備 (Arrange) ---
        Long movieId = 1L;
        String username = "testuser";
        
        // モック用のデータ作成
        Movie mockMovie = new Movie();
        mockMovie.setId(movieId);
        mockMovie.setReleaseDate(LocalDate.now().minusDays(1)); // 昨日公開（公開済み）

        User mockUser = new User();
        mockUser.setUsername(username);

        // リポジトリが呼ばれた時の挙動を定義 (Stubbing)
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(mockMovie));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(awkwardnessVoteRepository.findByUserAndMovie(any(), any())).thenReturn(Optional.empty());

        // --- 実行 (Act) ---
        // 投票実行（気まずい=true, タグ指定なし）
        votingService.processVotes(movieId, username, true, List.of(), List.of());

        // --- 検証 (Assert) ---
        // 1. 気まずさ投票が1回 save されたか確認
        verify(awkwardnessVoteRepository, times(1)).save(any(AwkwardnessVote.class));
        
        // 2. タグ投票の削除処理が呼ばれたか確認（タグが空でも削除処理は走るロジックのため）
        verify(movieTagVoteRepository, times(2)).deleteAll(any());
    }
}