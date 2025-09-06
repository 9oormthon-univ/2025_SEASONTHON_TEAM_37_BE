package rebound.backend.utils;

public class NicknameMasker {

    public static String mask(String nickname) {
        // 규칙 1: 닉네임이 없는 경우
        if (nickname == null || nickname.isEmpty()) {
            return "";
        }

        // 규칙 2: 닉네임이 한 글자인 경우
        if (nickname.length() == 1) {
            return "익명";
        }

        // 규칙 3: 닉네임이 두 글자 이상인 경우
        // 첫 글자 + 나머지 글자 수만큼 '*' 추가
        return nickname.charAt(0) + "*".repeat(nickname.length() - 1);
    }
}
