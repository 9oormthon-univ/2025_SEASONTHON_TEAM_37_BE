package rebound.backend.domain.tag.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rebound.backend.domain.post.entity.Post;
import rebound.backend.domain.tag.entity.Tag;
import rebound.backend.domain.tag.repository.TagRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public Tag getOrCreate(String name) {
        String n = normalize(name);                // 트림/소문자화 등 정책
        return tagRepository.findByName(n).orElseGet(() -> {
            try {
                return tagRepository.save(Tag.builder().name(n).build());
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // 동시 생성 경합에서 한쪽이 UNIQUE로 막히면 재조회
                return tagRepository.findByName(n).orElseThrow(() -> e);
            }
        });
    }

    @Transactional
    public void applyTags(Post post, List<String> raw) {
        if (raw == null) { post.getTags().clear(); return; }
        Set<Tag> tags = raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::getOrCreate)           // ← 여기서 기존 tagId 재사용
                .collect(Collectors.toCollection(LinkedHashSet::new));
        post.getTags().clear();
        post.getTags().addAll(tags);
    }

    private String normalize(String s) {
        String t = s.trim();
        if (t.length() > 50) t = t.substring(0, 50);
        return t;
    }
}