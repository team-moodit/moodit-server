package com.team.moodit.api.controller.v1;

import com.team.moodit.domain.enums.ObjectResourceType;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.FileUploader;
import com.team.moodit.support.file.UploadResult;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FIleController {
    private final FileUploader fileUploader;

    @GetMapping("/v1/files/presigned-url")
    public ApiResponse<UploadResult> getPresignedUrl(
//            ApiUser apiUser,
            @RequestParam ObjectResourceType resourceType,
            @RequestParam String fileName
    ) {
        UploadResult result = fileUploader.createPresignedUrl(1L, resourceType, fileName);
        return ApiResponse.success(result);
    }
}
