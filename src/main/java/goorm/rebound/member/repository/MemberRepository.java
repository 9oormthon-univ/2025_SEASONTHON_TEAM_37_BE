package goorm.rebound.member.repository;

import goorm.rebound.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("""
    SELECT m
    FROM Member m
    WHERE m.loginId = :loginId
    """)
    Optional<Member> findByLoginId(@Param("loginId") String loginId);

    @Query("""
    SELECT m
    FROM Member m
    WHERE m.nickname = :nickname
    """)
    Optional<Member> findByNickname(@Param("nickname") String nickname);
}
