package rebound.backend.global.utils;

public class NicknameMasker {

    public static String mask(String name) {
        if (name == null || name.isEmpty()) {
            return ""; // 이름이 없으면 빈 문자열 반환
        }

        if (name.length() == 1) {
            return name; // 이름이 한 글자이면 그대로 반환
        }

        // 첫 글자 + 나머지 글자 수만큼 '*' 추가
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
