package com.team.moodit.support.file;

import com.team.moodit.domain.enums.ObjectResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class File {
    private Long id;
    private String url;
    private ObjectResourceType resourceType;
}
