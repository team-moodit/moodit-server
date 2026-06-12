package com.team.moodit.api.controller.v1;

import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.FileUploader;
import com.team.moodit.support.file.UploadResult;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FIleController {
    private final FileUploader fileUploader;

    @PostMapping("/v1/files/upload")
    public ApiResponse<UploadResult> uploadFile(
            ApiUser apiUser,
            @RequestPart MultipartFile file
    ) {
        UploadResult result = fileUploader.upload(apiUser.getId(), file);
        return ApiResponse.success(result);
    }
}
