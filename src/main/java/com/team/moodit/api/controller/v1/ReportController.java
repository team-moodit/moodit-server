package com.team.moodit.api.controller.v1;

import com.team.moodit.api.assembler.ReportAssembler;
import com.team.moodit.api.controller.v1.response.ReportResponse;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportAssembler reportAssembler;

    @GetMapping("/v1/reports")
    public ApiResponse<ReportResponse> getReport(
//            ApiUser apiUser
    ) {
        return ApiResponse.success(reportAssembler.getReport(new ApiUser(1L)));
    }
}
