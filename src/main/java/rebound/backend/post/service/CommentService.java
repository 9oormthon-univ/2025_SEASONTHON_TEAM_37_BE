package rebound.backend.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rebound.backend.member.domain.Member;
import rebound.backend.member.domain.MemberImage;
import rebound.backend.member.repository.MemberImageRepository;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.post.dto.CommentResponse;
import rebound.backend.post.entity.*;
import rebound.backend.post.repository.CommentReactionRepository;
import rebound.backend.post.repository.CommentRepository;
import rebound.backend.utils.InteractionAuth;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final CommentReactionRepository reactionRepo;
    private final MemberRepository memberRepository;
    private final MemberImageRepository memberImageRepository;

    private Long me() { return InteractionAuth.currentMemberId(); }

    /**
     * 댓글 목록 조회 (컴파일 오류 수정된 최종본)
     */
    public Slice<CommentResponse> list(Long postId, int page, int size) {
        // 1. 댓글 목록을 엔티티로 조회 (쿼리 1)
        PageRequest pageable = PageRequest.of(page, size);
        Slice<Comment> commentSlice = commentRepo.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, CommentStatus.PUBLIC, pageable);

        List<Comment> comments = commentSlice.getContent();
        if (comments.isEmpty()) {
            // [오류 1 수정] Slice.empty() 대신 명시적으로 SliceImpl 반환
            return new SliceImpl<>(Collections.emptyList(), pageable, commentSlice.hasNext());
        }

        // 2. *모든* 댓글의 작성자 ID를 추출 (익명 포함)
        List<Long> allMemberIds = comments.stream()
                .map(Comment::getMemberId)
                .distinct()
                .toList();

        // 3. *모든* 작성자들의 Member 정보를 조회 (쿼리 2)
        // [오류 2 수정] 'findAllByIdIn' -> 'findAllById' (JPA 기본 제공 메서드)
        Map<Long, Member> memberMap = memberRepository.findAllById(allMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // 4. *모든* 작성자들의 이미지 정보를 조회 (쿼리 3)
        // [오류 3, 4 수정] 'findAllByMemberIdIn' -> 'findAllByMember_IdIn' (Repo에 새로 추가한 메서드)
        Map<Long, MemberImage> imageMap = memberImageRepository.findAllByMember_IdIn(allMemberIds).stream()
                .collect(Collectors.toMap(img -> img.getMember().getId(), img -> img));

        // 5. 엔티티 목록을 DTO로 변환
        List<CommentResponse> dtoList = comments.stream()
                .map(comment -> {
                    Member member = memberMap.get(comment.getMemberId());
                    MemberImage image = imageMap.get(comment.getMemberId());

                    // DTO 팩토리 메서드가 수정된 로직으로 최종 DTO를 생성
                    return CommentResponse.from(comment, member, image);
                })
                .toList();

        // 6. 최종 Slice로 반환 (이것이 Slice<CommentResponse> 타입)
        return new SliceImpl<>(dtoList, commentSlice.getPageable(), commentSlice.hasNext());
    }

    // 댓글 생성
    @Transactional
    public Comment create(Long postId, String content, boolean isAnonymous, Long parentId) {
        var c = Comment.builder()
                .postId(postId)
                .memberId(me())
                .content(content)
                .isAnonymous(isAnonymous)
                .parentCommentId(parentId)
                .status(CommentStatus.PUBLIC)
                .build();
        return commentRepo.save(c);
    }

    // 조건부 삭제
    @Transactional
    public void delete(Long commentId) {
        Long m = me();
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));
        if (!c.getMemberId().equals(m)) {
            throw new IllegalArgumentException("내가 작성한 댓글만 삭제할 수 있습니다.");
        }

        boolean hasChildren = commentRepo.existsByParentCommentIdAndStatusNot(
                commentId, CommentStatus.DELETED);

        if (hasChildren) {
            commentRepo.softDelete(commentId);
        } else {
            commentRepo.deleteById(commentId);
        }
    }

    // 하트토글 및 like_count 동기화
    @Transactional
    public boolean toggleHeart(Long commentId) {
        Long m = me();
        boolean existed = reactionRepo.existsByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);

        if (existed) {
            reactionRepo.deleteByCommentIdAndMemberIdAndType(commentId, m, ReactionType.HEART);
            syncLikeCount(commentId);
            return false;
        } else {
            try {
                reactionRepo.save(
                        CommentReaction.builder()
                                .commentId(commentId)
                                .memberId(m)
                                .type(ReactionType.HEART)
                                .build()
                );
                syncLikeCount(commentId);
                return true;
            } catch (DataIntegrityViolationException dup) {
                // 거의 동시에 같은 사용자 요청이 들어온 경우 → 이미 만들어졌다고 보고 켜진 상태 반환
                syncLikeCount(commentId);
                return true;
            }
        }
    }

    public long countHearts(Long commentId) {
        return reactionRepo.countByCommentIdAndType(commentId, ReactionType.HEART);
    }

    // 현재 reaction 집계값으로 like_count 세팅
    private void syncLikeCount(Long commentId) {
        int cnt = (int) reactionRepo.countByCommentIdAndType(commentId, ReactionType.HEART);
        commentRepo.updateLikeCount(commentId, cnt);
    }
}