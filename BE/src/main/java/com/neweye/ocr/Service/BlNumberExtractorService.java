package com.neweye.ocr.Service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BlNumberExtractorService {

    /*
     * 라벨 근접 매칭
     */
    private static final Pattern NEAR_LABEL = Pattern.compile(
    "(?is)" +  // DOTALL + CASE_INSENSITIVE
    "(?:B\\/?L\\.?\\s*No\\.?|Bill\\s*of\\s*Lading\\s*No\\.?|Air\\s*Waybill|Waybill\\s*No\\.?|MAWB\\s*No\\.?|HAWB\\s*No\\.?|H(?:ouse)?\\s*(?:B\\/?L|BL)\\s*No\\.?)" +
    "\\s*(?:[\\.:：#-]*\\s*)" +   // 콜론(:/전각：), 점, 대시 등 자유롭게 허용 + 줄바꿈 포함
    "(" +
      "[A-Z]{1,4}/\\d{2}/\\d{5,}" +                             // MI/24/304058
    "|" +
      "[A-Z]{2,10}[\\s-]?[A-Z0-9]{1,10}" +                     // 공백/하이픈 허용, 영숫자 혼합 (CHLY720Q013, AEBDEL36, MEI 1255061)
    "|" +
      "\\d{3}[-\\s]?(?:\\d{8}|\\d{4}[-\\s]?\\d{4}|\\d{3}[-\\s]?\\d{5})" +  // MAWB: 3 + (8연속 or 4+4 or 3+5)
    ")"
    );

    /*
     * 전역 매칭 (백업), 라벨 근접 매칭에서 찾지 못할 경우를 대비
     */
    private static final Pattern GLOBAL = Pattern.compile(
    "(?i)" +
    "(?<![A-Z0-9])" +
    "(" +
      "[A-Z]{1,4}/\\d{2}/\\d{5,}" +
    "|" +
      "[A-Z]{2,6}[\\s-]?\\d{6,10}" +                // 전역은 여전히 숫자 최소 6자리
    "|" +
      "\\d{3}[-\\s]?(?:\\d{8}|\\d{4}[-\\s]?\\d{4}|\\d{3}[-\\s]?\\d{5})" +
    ")" +
    "(?![A-Z0-9])"
    );

    /*
     * B/L 넘버 추출
     */
    public List<String> extractBlNumbers(String ocrText) {
        String text = normalize(ocrText);

        //? 후보 수집 (중복 제거 위해 LinkedHashMap을 사용, 중복된 값 제거)
        Map<String, Candidate> candidates = new LinkedHashMap<>();

        //? 1) 라벨 근접 매칭: 가산점 +2
        Matcher m1 = NEAR_LABEL.matcher(text);
        while (m1.find()) {
            String raw = m1.group(1);
            String norm = normalizeBl(raw);
            if (norm.isEmpty()) continue;
            //? 유효성 검사
            if (!isValidBlNumber(norm)) continue;

            Candidate c = candidates.getOrDefault(norm, new Candidate(norm));
            c.score += 2;
            c.fromLabel = true;
            candidates.put(norm, c);
        }

        //? 2) 전역 매칭: 가산점 +0 (백업용)
        Matcher m2 = GLOBAL.matcher(text);
        while (m2.find()) {
            String raw = m2.group(1);
            String norm = normalizeBl(raw);
            if (norm.isEmpty()) continue;
            if (!isValidBlNumber(norm)) continue;

            Candidate c = candidates.getOrDefault(norm, new Candidate(norm));
            candidates.put(norm, c);
        }

        //? 3) 패턴 적합도 스코어링 (길이/형태 가중치)
        for (Candidate c : candidates.values()) {
            c.score += patternScore(c.value);
        }

        //? 4) 스코어 내림차순, 동일하면 먼저 나온 것 우선
        List<Candidate> sorted = new ArrayList<>(candidates.values());
        sorted.sort((a, b) -> Integer.compare(b.score, a.score));

        List<String> result = new ArrayList<>();
        for (Candidate c : sorted) result.add(c.value);
        return result;
    }

    /*
     * 주요 B/L 넘버 추출
     */
    public String extractPrimaryBlNumber(String ocrText) {
        List<String> list = extractBlNumbers(ocrText);
        return list.isEmpty() ? "B/L 넘버를 찾을 수 없습니다." : list.get(0);
    }

    /*
     * B/L 넘버 추출 (우선순위순)
     */
    public String extractBlNumbersOnly(String ocrText) {
        List<String> list = extractBlNumbers(ocrText);
        if (list.isEmpty()) return "B/L 넘버를 찾을 수 없습니다.";
        StringBuilder sb = new StringBuilder("=== 추출된 B/L 넘버(우선순위순) ===\n");
        for (int i = 0; i < list.size(); i++) {
            sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
        }
        return sb.toString();
    }

    /*
     * 패턴 매칭 디버깅
     */
    public String debugPatternMatching(String ocrText) {
        String text = normalize(ocrText);
        StringBuilder sb = new StringBuilder();

        sb.append("=== NEAR_LABEL 패턴 ===\n");
        sb.append(NEAR_LABEL.pattern()).append("\n\n");
        Matcher m1 = NEAR_LABEL.matcher(text);
        boolean found1 = false;
        while (m1.find()) {
            found1 = true;
            sb.append("매치: ").append(m1.group()).append("\n");
            sb.append("  그룹1: ").append(m1.group(1)).append("\n");
        }
        if (!found1) sb.append("매치 없음\n");
        sb.append("\n");

        sb.append("=== GLOBAL 패턴 ===\n");
        sb.append(GLOBAL.pattern()).append("\n\n");
        Matcher m2 = GLOBAL.matcher(text);
        boolean found2 = false;
        while (m2.find()) {
            found2 = true;
            sb.append("매치: ").append(m2.group()).append("\n");
            sb.append("  그룹1: ").append(m2.group(1)).append("\n");
        }
        if (!found2) sb.append("매치 없음\n");

        return sb.toString();
    }

    /* ===================== 유틸/검증 ===================== */

    /*
     * 정규화
     */
    private static String normalize(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");            // zero-width 제거
        t = t.replaceAll("[\\u00A0\\u2007\\u202F]", " ");            // NBSP류 → 스페이스
        t = t.replaceAll("[\\u2010-\\u2015\\u2212\\uFE58\\uFE63\\uFF0D]", "-"); // 유니코드 대시 → '-'
        return t;
    }

    /*
     * B/L 넘버 정규화
     */
    private static String normalizeBl(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("\\s+", "");
        // 슬래시형: MI/24/304058 → MI24304058 로 통일(원문 보존 필요 시 변경)
        if (s.matches("(?i)[A-Z]{1,4}/\\d{2}/\\d{5,}")) {
            s = s.replace("/", "");
        }
        return s;
    }

    /*
     * 최종 유효성 검사: "진짜 같아 보이는" 것만 통과
     */
    private static boolean isValidBlNumber(String bl) {
        String s = bl.trim();
    
        //? 숫자-only
        if (s.matches("^\\d+$")) return false;
    
        //? 길이, 최소 6자리, 최대 20자리
        if (s.length() < 6 || s.length() > 20) return false;
    
        //? 영문+숫자 모두 포함
        if (!s.matches(".*[A-Z].*") || !s.matches(".*\\d.*")) return false;
    
        //? 보통 숫자로 끝남, 숫자로 끝나지 않으면 통과 불가
        if (!s.matches(".*\\d$")) return false;
    
        //? 컨테이너 번호(4문자+7숫자) 제외
        if (s.matches("(?i)^[A-Z]{4}\\d{7}$")) return false;
    
        //? 대표 허용 포맷, 대표 포맷이 아니면 통과 불가
        if (s.matches("(?i)^[A-Z]{1,4}/\\d{2}/\\d{5,}$")) return true; // MI/24/304058 → MI24304058
        if (s.matches("^\\d{3}[-\\s]?(?:\\d{8}|\\d{4}[-\\s]?\\d{4}|\\d{3}[-\\s]?\\d{5})$")) return true; // MAWB
    
        //? 일반 알파+숫자 혼합 꼬리(라벨 근접에서 흔함): CHLY720Q013, MJNGB24060093, MJLS-2406034, MEI1255061, AEBDEL36 등
        if (s.matches("(?i)^[A-Z]{2,10}-?[A-Z0-9]{1,12}$")) return true;
    
        return false;
    }
    

    /*
     * 간단 스코어링: 라벨 가산점 외에 길이/형태 적합도 보정
     */
    private static int patternScore(String s) {
        int score = 0;
        int len = s.length();

        if (len >= 10 && len <= 12) score += 1; //? 이상적 길이(10~12) 가산

        //? 강한 패턴일수록 가산
        if (s.matches(
            "(?i)" +  //? 대소문자 구분 안함
            "^"+ //? 문자열 시작
            "[A-Z]{3,4}"+ //? 대문자, 3~4자리
            "[A-Z0-9]{0,4}"+ //? 대문자 또는 숫자, 0~4자리
            "\\d{6,}$")) score += 2; //? 숫자, 6자리 이상
        else if (s.matches(
            "(?i)" +  //? 대소문자 구분 안함
            "^[A-Z]"+ //? 대문자로 시작
            "{2,6}"+ //? 2~6자리
            "-?\\d{6,10}$")) score += 1; //? 숫자, 6~10자리

        // 하이픈/공백 등 잡스런 문자 거의 없으면 가산
        // if (!s.matches(".*[^A-Z0-9].*")) score += 1;

        return score;
    }

    /*
     * 전화번호 판별
     */
    private static boolean looksLikePhone(String s) {
        String compact = s.replaceAll("[\\s-]", ""); //? 하이픈/공백 제거
        if (compact.matches("^0(?:10|11|16|17|18|19)\\d{7,8}$")) return true; //? 국내 휴대폰 (010/011/016/017/018/019) 10~11자리
        return false; //? 국제전화 + 국가번호 패턴 등 필요하면 추가
    }

    /*
     * 내부용 후보 구조체
     */
    private static class Candidate {
        final String value;
        boolean fromLabel = false; //? 라벨 근접 매칭 여부
        int score = 0;
        Candidate(String value) { this.value = value; }
    }
}
