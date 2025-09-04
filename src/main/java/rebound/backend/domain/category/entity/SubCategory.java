package rebound.backend.domain.category.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SubCategory {

    // 1. 입시
    ADMISSION_STRATEGY_ERROR("1.1", "수시/정시 전략 오류", MainCategory.ADMISSION),
    REQUIREMENT_MISUNDERSTANDING("1.2", "전형 요건 오해", MainCategory.ADMISSION),
    POOR_SELF_INTRO("1.3", "자기소개서/활동증빙 부실", MainCategory.ADMISSION),
    POOR_ESSAY_INTERVIEW("1.4", "논술/면접 준비 미흡", MainCategory.ADMISSION),
    APPLICATION_MISTAKE("1.5", "원서 접수 실수", MainCategory.ADMISSION),
    REGRET_MAJOR_CHOICE("1.6", "학과 선택 후회", MainCategory.ADMISSION),
    FAILED_RETRY_STRATEGY("1.7", "재수/반수 전략 실패", MainCategory.ADMISSION),
    ADMISSION_ETC("1.8", "기타", MainCategory.ADMISSION),

    // 2. 학업·연구
    PAPER("2.1", "논문", MainCategory.STUDY),
    PROJECT("2.2", "프로젝트", MainCategory.STUDY),
    TEAM_ASSIGNMENT("2.3", "팀과제", MainCategory.STUDY),
    DELAYED_GRADUATION("2.4", "졸업지연", MainCategory.STUDY),
    LEAVE_OF_ABSENCE("2.5", "휴학/자퇴", MainCategory.STUDY),
    STUDY_ETC("2.6", "기타", MainCategory.STUDY),

    // 3. 해외·이주·언어
    STUDY_ABROAD_FAIL("3.1", "유학/교환 적응 실패", MainCategory.ABROAD),
    FINANCIAL_PROBLEM("3.2", "금전문제", MainCategory.ABROAD),
    LANGUAGE_BARRIER("3.3", "언어장벽", MainCategory.ABROAD),
    CULTURE_SHOCK("3.4", "문화충격", MainCategory.ABROAD),
    ABROAD_ETC("3.5", "기타", MainCategory.ABROAD),

    // 4. 취업준비
    JOB_SELF_INTRO("4.1", "자소서/이력서", MainCategory.JOB_PREP),
    JOB_PORTFOLIO("4.2", "포트폴리오", MainCategory.JOB_PREP),
    COMPANY_ROLE_ANALYSIS_POOR("4.3", "기업·직무 분석 미흡", MainCategory.JOB_PREP),
    RANDOM_APPLICATION("4.4", "무분별 지원/미스핏 공고", MainCategory.JOB_PREP),
    DEADLINE_MISS("4.5", "데드라인 놓침", MainCategory.JOB_PREP),
    APTITUDE_CODING_TEST_FAIL("4.6", "인적성/코딩테스트 실패", MainCategory.JOB_PREP),
    INTERVIEW_PREP_MANNERS_FAIL("4.7", "면접 준비 부족/매너 실수", MainCategory.JOB_PREP),
    INTERN_STRATEGY_FAIL("4.8", "인턴 지원 전략 실패", MainCategory.JOB_PREP),
    JOB_PREP_ETC("4.9", "기타", MainCategory.JOB_PREP),

    // 5. 직장생활·협업
    TEAM_CONFLICT("5.1", "팀갈등", MainCategory.WORK),
    COMMUNICATION_ERROR("5.2", "커뮤니케이션 오류", MainCategory.WORK),
    REMOTE_WORK_FAIL("5.3", "원격근무 실패", MainCategory.WORK),
    REPORT_FEEDBACK("5.4", "보고/피드백", MainCategory.WORK),
    LOW_PERFORMANCE("5.5", "성과 미흡", MainCategory.WORK),
    JOB_CHANGE("5.6", "이직", MainCategory.WORK),
    RESIGNATION("5.7", "퇴사", MainCategory.WORK),
    WORK_ETC("5.8", "기타", MainCategory.WORK),

    // 6. 창업·재도전
    BM_MISS("6.1", "BM 미스", MainCategory.STARTUP),
    TEAM_BUILDING_FAIL("6.2", "팀빌딩 실패", MainCategory.STARTUP),
    CASH_BURNOUT("6.3", "자금소진", MainCategory.STARTUP),
    PIVOT_FAIL("6.4", "피벗 실패", MainCategory.STARTUP),
    RETRY_AFTER_CLOSURE("6.5", "폐업 후 재시도", MainCategory.STARTUP),
    STARTUP_ETC("6.6", "기타", MainCategory.STARTUP),

    // 7. 건강·멘탈
    BURNOUT("7.1", "번아웃", MainCategory.HEALTH),
    SLUMP("7.2", "슬럼프", MainCategory.HEALTH),
    SLEEP_RHYTHM_BREAK("7.3", "수면/리듬 붕괴", MainCategory.HEALTH),
    JOB_STRESS("7.4", "직무 스트레스", MainCategory.HEALTH),
    HEALTH_ETC("7.5", "기타", MainCategory.HEALTH),

    // 8. 대인관계·연애
    RELATIONSHIP_BREAK("8.1", "관계 단절", MainCategory.RELATIONSHIP),
    BOUNDARY_FAIL("8.2", "경계설정 실패", MainCategory.RELATIONSHIP),
    COMMUNICATION_MISUNDERSTANDING("8.3", "커뮤니케이션 오해", MainCategory.RELATIONSHIP),
    RELATION_ETC("8.4", "기타", MainCategory.RELATIONSHIP),

    // 9. 재무·투자
    STOCK_COIN_LOSS("9.1", "주식/코인 손실", MainCategory.FINANCE),
    OVER_LEVERAGE("9.2", "무리한 레버리지", MainCategory.FINANCE),
    HIGH_RETURN_FRAUD("9.3", "고수익 미끼/사기", MainCategory.FINANCE),
    LOAN_CREDIT_FAIL("9.4", "대출·신용관리 실패", MainCategory.FINANCE),
    BUDGET_SAVING_FAIL("9.5", "예산/저축 계획 실패", MainCategory.FINANCE),
    TAX_REPORT_MISTAKE("9.6", "세금·신고 실수", MainCategory.FINANCE),
    FINANCE_ETC("9.7", "기타", MainCategory.FINANCE),

    // 10. 기타
    ETC_ONLY("10.1", "기타", MainCategory.ETC);

    private final String code;
    private final String label;
    private final MainCategory mainCategory;

    SubCategory(String code, String label, MainCategory mainCategory) {
        this.code = code;
        this.label = label;
        this.mainCategory = mainCategory;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public MainCategory getMainCategory() { return mainCategory; }

    /** 메인 카테고리별 하위 목록 */
    public static List<SubCategory> byMain(MainCategory main) {
        return Arrays.stream(values())
                .filter(sc -> sc.mainCategory == main)
                .collect(Collectors.toList());
    }

    /** "4.7" 같은 코드로 찾기 */
    public static SubCategory fromCode(String code) {
        return Arrays.stream(values())
                .filter(sc -> sc.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown SubCategory code: " + code));
    }
}