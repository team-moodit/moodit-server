package com.team.moodit.api.controller.v1.request;

import java.util.List;

public record MatchCreateRequest(
   String title,
   List<Long> images
) {
}
