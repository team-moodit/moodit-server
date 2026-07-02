package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.AddReviewRequest;
import com.team.moodit.domain.review.ReviewService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/v1/reviews")
    public ApiResponse<?> createReview(
            ApiUser apiUser,
            @RequestBody AddReviewRequest request
    ) {
        reviewService.addReview(apiUser, request.toTarget(), request.toContent());
        return ApiResponse.success();
    }
}
