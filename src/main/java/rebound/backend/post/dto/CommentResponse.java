package rebound.backend.post.dto;

import rebound.backend.member.domain.Member;
import rebound.backend.member.domain.MemberImage;
import rebound.backend.post.entity.Comment;
import rebound.backend.post.entity.CommentStatus;
import rebound.backend.utils.NicknameMasker;

import java.time.Instant;

/**
 * 댓글 조회 응답 DTO
 */
public record CommentResponse(
        Long commentId,
        Long postId,
        AuthorInfo author, // 작성자 정보 (요청하신 필드 포함)
        String content,
        boolean isAnonymous,
        Long parentCommentId,
        int likeCount,
        CommentStatus status,
        Instant createdAt
) {

    public record AuthorInfo(Long memberId, String nickname, String imageUrl) {}

    public static CommentResponse from(Comment comment, Member member, MemberImage memberImage) {

        // 1. 회원 기본 정보 설정 (탈퇴 회원 등 예외 처리)
        Long authorId = comment.getMemberId();
        String realNickname = "(알 수 없음)";
        if (member != null) {
            authorId = member.getId();
            realNickname = member.getNickname();
        }

        // 2. 이미지 URL 설정 (요구사항: 익명 여부와 관계없이 원본 반환)
        String imageUrl = (memberImage != null) ? memberImage.getImageUrl() : null;

        // 3. 표시될 닉네임 결정 (요구사항: 익명일 때만 마스킹)
        String displayName = comment.isAnonymous() ? NicknameMasker.mask(realNickname) : realNickname;

        // 4. DTO 생성 (memberId와 imageUrl은 항상 원본 값 사용)
        AuthorInfo authorInfo = new AuthorInfo(authorId, displayName, imageUrl);

        return new CommentResponse(
                comment.getCommentId(),
                comment.getPostId(),
                authorInfo,
                comment.getContent(),
                comment.isAnonymous(),
                comment.getParentCommentId(),
                comment.getLikeCount(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}
