package com.team.moodit.support.file;

import com.team.moodit.domain.enums.ObjectResourceType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ObjectKeyGenerator {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 예) 자원명/20260619/dniqwjdasw.{ext} 형태로 ObjectKey를 생성
     */
    public String generate(ObjectResourceType resourceType, String extension) {
        String date = LocalDate.now(KST).format(DATE_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "");

        return "%s/%s/%s.%s".formatted(resourceType.name().toLowerCase(), date, fileName, extension);
    }
}
