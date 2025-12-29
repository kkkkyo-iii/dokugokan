package com.example.demo.voting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.BusinessLogicException;
import com.example.demo.movie.Movie;
import com.example.demo.movie.MovieInformationRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.voting.dto.TagVoteCountDto;
import com.example.demo.voting.dto.UserVoteStatusDto;
import com.example.demo.voting.dto.VoteResultDto;

@Service
public class VotingService {

    // 投票処理に必要なすべてのリポジトリをDI
    private final MovieInformationRepository movieRepository;
    private final UserRepository userRepository;
    private final AwkwardnessVoteRepository awkwardnessVoteRepository;
    private final MovieTagVoteRepository movieTagVoteRepository;
    private final TagRepository tagRepository;

    public VotingService(MovieInformationRepository movieRepository,
                         UserRepository userRepository,
                         AwkwardnessVoteRepository awkwardnessVoteRepository,
                         MovieTagVoteRepository movieTagVoteRepository,
                         TagRepository tagRepository) {
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.awkwardnessVoteRepository = awkwardnessVoteRepository;
        this.movieTagVoteRepository = movieTagVoteRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * フォームから送信されたすべての投票を処理するメインメソッド
     */
    @Transactional // 複数のDB操作を安全に実行（途中で失敗したら全部元に戻す）
    public void processVotes(Long movieId, String username, boolean isAwkward,
                             List<Long> awkwardReasonTagIds, List<Long> impressionTagIds) {

        // 1. 必要なエンティティを取得
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("映画が見つかりません"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        //  公開日チェック
        if (!isMovieReleased(movie)) {
            throw new BusinessLogicException("この映画はまだ公開されていないため、投票できません。");
        }

        // 3. 「気まずさ」投票の処理 (更新または新規作成)
        processAwkwardnessVote(user, movie, isAwkward);

        // 4. 「気まずい理由」タグの処理
        processTagVotes(user, movie, awkwardReasonTagIds, TagVoteType.AWKWARD_REASON);

        // 5. 「読後感」タグの処理
        processTagVotes(user, movie, impressionTagIds, TagVoteType.IMPRESSION);
    }

    /**
     * 映画が公開済みかチェックする (明日以降はfalse)
     */
    private boolean isMovieReleased(Movie movie) {
        LocalDate today = LocalDate.now();
        if (movie.getReleaseDate() == null || movie.getReleaseDate().isAfter(today)) {
            return false;
        }
        return true;
    }

    /**
     * 「気まずさ」投票をDBに保存・更新する
     */
    private void processAwkwardnessVote(User user, Movie movie, boolean isAwkward) {
        // 既に投票済みか検索し、存在しなければ新しい投票を作成
        AwkwardnessVote vote = awkwardnessVoteRepository.findByUserAndMovie(user, movie)
                .orElse(new AwkwardnessVote()); 

        vote.setUser(user);
        vote.setMovie(movie);
        vote.setAwkward(isAwkward); // 投票内容をセット (または更新)
        awkwardnessVoteRepository.save(vote);
    }

    /**
     * 「タグ」投票をDBに保存・更新する (既存の投票を一旦削除し、新しい投票を保存)
     */
    private void processTagVotes(User user, Movie movie, List<Long> tagIds, TagVoteType voteType) {
        // 1. この映画に対する、このユーザーの、このタイプの古い投票を取得
        List<MovieTagVote> oldVotes = movieTagVoteRepository.findByUserAndMovieAndVoteType(user, movie, voteType);
        
        // 古い投票のタグのカウントを減らす
        for (MovieTagVote vote : oldVotes) {
            // ここで tagRepository.save(tag) ではなく、専用メソッドを呼ぶ
            tagRepository.decrementVoteCount(vote.getTag().getId());
        }
        
        // 古い投票レコードを削除
        movieTagVoteRepository.deleteAll(oldVotes);

        // 2. tagIdsがnullや空でない場合、新しい投票を保存
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);
            
            // ★追加: 新しい投票のタグのカウントを増やす
            for (Tag tag : tags) {
                tagRepository.incrementVoteCount(tag.getId());
            }

            List<MovieTagVote> newVotes = tags.stream().map(tag -> {
                MovieTagVote newVote = new MovieTagVote();
                newVote.setUser(user);
                newVote.setMovie(movie);
                newVote.setTag(tag);
                newVote.setVoteType(voteType);
                return newVote;
            }).collect(Collectors.toList());

            movieTagVoteRepository.saveAll(newVotes);
        }
    }
    /**
     * 映画IDを指定して、全ての投票結果を集計する
     * @param movieId 映画ID
     * @return 集計結果DTO
     */
    public VoteResultDto getVoteResults(Long movieId) {
        VoteResultDto results = new VoteResultDto();
        
        // 1. 映画エンティティを取得 (RepositoryのcountメソッドがMovieオブジェクトを必要とするため)
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("映画が見つかりません"));

        // 2. 「気まずさ」の割合を計算
        long awkwardCount = awkwardnessVoteRepository.countByMovieAndAwkward(movie, true);
        long notAwkwardCount = awkwardnessVoteRepository.countByMovieAndAwkward(movie, false);
        long totalAwkwardVotes = awkwardCount + notAwkwardCount;
        
        results.setTotalAwkwardVotes(totalAwkwardVotes);
        if (totalAwkwardVotes > 0) {
            // Math.roundで四捨五入してパーセンテージを計算
            results.setAwkwardPercent((int) Math.round((double) awkwardCount * 100 / totalAwkwardVotes));
            results.setNotAwkwardPercent((int) Math.round((double) notAwkwardCount * 100 / totalAwkwardVotes));
        }

        // 3. 「気まずい理由」タグを取得 (Top 3 + それ以降)
        List<TagVoteCountDto> awkwardReasonTags = movieTagVoteRepository.findTagVoteCountsByMovie(
            movieId, TagVoteType.AWKWARD_REASON
        );
        results.setAwkwardReasonTags(awkwardReasonTags);

        // 4. 「読後感」タグを取得 (Top 5 + それ以降)
        List<TagVoteCountDto> impressionTags = movieTagVoteRepository.findTagVoteCountsByMovie(
            movieId, TagVoteType.IMPRESSION
        );
        results.setImpressionTags(impressionTags);

        return results;
    }
    /**
     * ユーザーと映画を指定し、そのユーザーの現在の投票状態を取得する
     * @param user ログイン中のユーザー
     * @param movie 対象の映画
     * @return ユーザーの投票状態DTO
     */
    public UserVoteStatusDto getUserVoteStatus(User user, Movie movie) {
        UserVoteStatusDto dto = new UserVoteStatusDto();
        
        // 1. 「気まずさ」投票の状態を取得
        Optional<AwkwardnessVote> awkwardVoteOpt = awkwardnessVoteRepository.findByUserAndMovie(user, movie);
        if (awkwardVoteOpt.isPresent()) {
            dto.setAwkwardVote(awkwardVoteOpt.get().isAwkward());
            dto.setHasVoted(true); // 投票済みフラグ
        }

        // 2. 「気まずい理由」タグの投票状態を取得
        List<MovieTagVote> awkwardTags = movieTagVoteRepository.findByUserAndMovieAndVoteType(user, movie, TagVoteType.AWKWARD_REASON);
        if (!awkwardTags.isEmpty()) {
            Set<Long> tagIds = awkwardTags.stream()
                                .map(vote -> vote.getTag().getId())
                                .collect(Collectors.toSet());
            dto.setAwkwardReasonTagIds(tagIds);
            dto.setHasVoted(true); // 投票済みフラグ
        }

        // 3. 「読後感」タグの投票状態を取得
        List<MovieTagVote> impressionTags = movieTagVoteRepository.findByUserAndMovieAndVoteType(user, movie, TagVoteType.IMPRESSION);
        if (!impressionTags.isEmpty()) {
            Set<Long> tagIds = impressionTags.stream()
                                .map(vote -> vote.getTag().getId())
                                .collect(Collectors.toSet());
            dto.setImpressionTagIds(tagIds);
            dto.setHasVoted(true); // 投票済みフラグ
        }

        return dto;
    }
}