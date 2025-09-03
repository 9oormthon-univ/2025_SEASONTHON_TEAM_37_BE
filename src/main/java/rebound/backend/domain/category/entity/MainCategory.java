package rebound.backend.domain.category.entity;

public enum MainCategory {
    ADMISSION("1", "입시"),
    STUDY("2", "학업·연구"),
    ABROAD("3", "해외·이주·언어"),
    JOB_PREP("4", "취업준비"),
    WORK("5", "직장생활·협업"),
    STARTUP("6", "창업·재도전"),
    HEALTH("7", "건강·멘탈"),
    RELATIONSHIP("8", "대인관계·연애"),
    FINANCE("9", "재무·투자"),
    ETC("10", "기타");

    private final String code;
    private final String label;

    MainCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
}