package rebound.backend.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.member.domain.MemberImage;

import java.util.Optional;

public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {
    @Query("""
    SELECT mI
    FROM MemberImage mI
    WHERE mI.member.id = :memberId
    """)
    Optional<MemberImage> findImageByMemberId(@Param("memberId") Long memberId);
}
