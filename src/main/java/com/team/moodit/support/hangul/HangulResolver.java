package com.team.moodit.support.hangul;

public class HangulResolver {
    public static String resolveParticle(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // 단어의 마지막 글자 추출
        char lastChar = word.charAt(word.length() - 1);

        // 마지막 글자가 완전한 한글 음절(가~힣)인지 확인
        if (lastChar < 0xAC00 || lastChar > 0xD7A3) {
            return word + "(을)를";
        }

        // 받침 유무 판별식
        int jongseong = (lastChar - 0xAC00) % 28;

        if (jongseong > 0) {
            return word + "을";
        } else {
            return word + "를";
        }
    }
}
