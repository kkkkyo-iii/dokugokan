package com.example.demo.movie;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.voting.TagRepository;
import com.example.demo.voting.dto.UserVoteStatusDto;
import com.example.demo.voting.dto.VoteResultDto;

import reactor.core.publisher.Mono;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    // GlobalControllerAdviceの依存関係
    @MockitoBean
    private TagRepository tagRepository;

    // ★追加: GlobalControllerAdviceのもう一つの依存関係
    @MockitoBean
    private TrendingMovieRepository trendingMovieRepository;

    @Test
    @DisplayName("映画詳細ページへのアクセスが成功し、正しいViewが返されること")
    void showMovieDetails_Success() throws Exception {
        // --- 準備 (Arrange) ---
        Integer tmdbId = 123;
        Movie mockMovie = new Movie();
        mockMovie.setTmdbId(tmdbId);
        mockMovie.setTitle("Test Movie");

        // Serviceが返すデータを完全な状態でモック化する
        Map<String, Object> mockDetailData = new HashMap<>();
        mockDetailData.put("movie", mockMovie);
        
        // HTML(Thymeleaf)が参照する変数をセット
        mockDetailData.put("isFavorited", false);
        mockDetailData.put("awkwardReasonTagGroups", Collections.emptyMap());
        mockDetailData.put("impressionTagGroups", Collections.emptyMap());
        mockDetailData.put("voteResults", new VoteResultDto());
        mockDetailData.put("userVoteStatus", new UserVoteStatusDto());

        
        given(movieService.findOrCreateMovieByTmdbId(tmdbId)).willReturn(Mono.just(mockMovie));
        
        
        given(movieService.getMovieDetailData(eq(mockMovie), any())).willReturn(mockDetailData);

        MvcResult mvcResult = mockMvc.perform(get("/movie/" + tmdbId)
                .with(user("testuser").roles("USER")))
                .andExpect(request().asyncStarted()) // 非同期処理が始まったか？
                .andReturn();

        //  非同期処理の結果を受け取ってから (asyncDispatch)、Viewの検証を行う
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/detail"))
                .andExpect(model().attributeExists("movie"))
                .andExpect(model().attribute("movie", mockMovie));
    }
}