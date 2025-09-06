package rebound.backend.member.service;

import rebound.backend.category.entity.MainCategory;
import rebound.backend.member.domain.Interest;
import rebound.backend.member.domain.Member;
import rebound.backend.member.domain.MemberImage;
import rebound.backend.member.dtos.requests.JoinRequest;
import rebound.backend.member.dtos.requests.MemberModifyRequest;
import rebound.backend.member.dtos.responses.JoinResponse;
import rebound.backend.member.dtos.requests.LoginRequest;
import rebound.backend.member.dtos.responses.LoginResponse;
import rebound.backend.member.dtos.responses.MyInfoResponse;
import rebound.backend.member.repository.MemberImageRepository;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.member.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rebound.backend.post.entity.Post;
import rebound.backend.post.entity.PostReaction;
import rebound.backend.post.repository.PostReactionRepository;
import rebound.backend.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MemberImageRepository memberImageRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;

    public JoinResponse join(JoinRequest joinRequest) {
        //loginId 중복 검사
        if (memberRepository.findByLoginId(joinRequest.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다");
        }

        //nickname 중복 검사
        if (memberRepository.findByNickname(joinRequest.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다");
        }

        //비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(joinRequest.getPassword());

        Member member = Member.builder()
                .loginId(joinRequest.getLoginId())
                .password_hash(hashedPassword)
                .nickname(joinRequest.getNickname())
                .age(joinRequest.getAge())
                .field(joinRequest.getField())
                .createdAt(LocalDateTime.now())
                .provider("LOCAL_LOGIN")
                .build();

        List<MainCategory> interests = joinRequest.getInterests();
        if (interests != null && !interests.isEmpty()) {
            interests.forEach(mainCategory -> {
                Interest interest = Interest.builder()
                        .mainCategory(mainCategory)
                        .build();
                member.addInterest(interest);
            });
        }

        Member savedMember = memberRepository.save(member);

        return new JoinResponse(savedMember.getLoginId(), savedMember.getId());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        //loginId로 조회
        Member member = memberRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다"));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword_hash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtUtil.createToken(member.getId());

        return new LoginResponse(member.getLoginId(), token);
    }

    public MyInfoResponse memberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id 의 회원이 존재하지 않습니다"));

        MemberImage memberImage = null; //최초 회원가입으로 이미지가 없을 경우 null

        if (memberImageRepository.findImageByMemberId(member.getId()).isPresent()) {
            //이미지가 있으면 출력
            memberImage = memberImageRepository.findImageByMemberId(member.getId()).get();
        }

        String imageUrl;
        if (memberImage != null) {
            imageUrl = memberImage.getImageUrl(); //프로필 이미지가 있으면 url 출력
        } else {
            imageUrl = ""; //없으면 빈 문자열
        }

        List<MainCategory> categories = new LinkedList<>();
        List<Interest> interests = member.getInterests();
        for (Interest interest : interests) {
            categories.add(interest.getMainCategory());
        }

        //hasRankingBadge 추가
        boolean hasRankingBadge = false;
        List<Post> memberPosts = postRepository.findPostsByMemberId(member.getId());
        if (memberPosts.isEmpty()) {
            hasRankingBadge = false;
        } else {
            for (Post memberPost : memberPosts) {
                List<PostReaction> memberPostLikes = postReactionRepository.countTotalLikeOfPost(memberPost.getPostId());
                if (memberPostLikes.size() >= 10) {
                    hasRankingBadge = true;
                }
            }
        }

        return new MyInfoResponse(member.getNickname(), member.getAge(), member.getField(),
                imageUrl, hasRankingBadge, member.getLoginId(), categories);
    }

    public void memberInfoModify(MemberModifyRequest request, Long memberId) {
        // 1. DB에서 영속성 컨텍스트가 관리하는 'member' 엔티티를 조회합니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id 의 회원이 존재하지 않습니다"));

        // 2. 조회한 'member' 객체의 필드를 직접 변경합니다. (더티 체킹)
        member.setNickname(request.getNickname());
        member.setAge(request.getAge());
        member.setField(request.getField());

        // 3. 관심사(Interests) 수정: 기존 컬렉션을 비우고 새로 추가합니다.
        // (CascadeType.ALL, orphanRemoval=true 설정이 Member 엔티티의 interests 필드에 있어야 완벽하게 동작합니다)
        member.getInterests().clear(); // 기존 관심사 삭제
        List<MainCategory> categories = request.getInterests();
        if (categories != null) {
            for (MainCategory category : categories) {
                Interest interest = Interest.builder()
                        .member(member) // 반드시 영속 상태인 member 객체를 참조해야 합니다.
                        .mainCategory(category)
                        .build();
                member.getInterests().add(interest); // 관리되는 컬렉션에 추가
            }
        }

        // 4. 프로필 이미지 수정 (이전 오류의 핵심 원인)
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            MemberImage existingImage = member.getMemberImage(); // 회원의 기존 이미지 조회

            if (existingImage != null) {
                // 4-1. 이미지가 이미 존재하면: URL만 업데이트합니다. (UPDATE 쿼리 발생)
                existingImage.setImageUrl(request.getImageUrl());
            } else {
                // 4-2. 이미지가 없으면: 새로 생성하고 관계를 설정합니다. (INSERT 쿼리 발생)
                MemberImage newImage = MemberImage.builder()
                        .member(member) // 영속 상태인 member 객체와 연결
                        .imageUrl(request.getImageUrl())
                        .build();
                memberImageRepository.save(newImage); // 새 이미지는 저장(persist)
                member.setMemberImage(newImage); // member 객체에도 관계 설정
            }
        }
    }
    }
