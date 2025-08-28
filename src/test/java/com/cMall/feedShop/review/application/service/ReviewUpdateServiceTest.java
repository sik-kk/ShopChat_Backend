package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewUpdateResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.exception.ReviewAccessDeniedException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewUpdateService 수정 기능 테스트")
class ReviewUpdateServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ReviewImageRepository reviewImageRepository;
    
    @Mock
    private ReviewImageService reviewImageService;
    
    @Mock
    private StorageService gcpStorageService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ReviewUpdateService reviewUpdateService;

    private ReviewUpdateRequest updateRequest;
    private User testUser;
    private Review testReview;

    @BeforeEach
    void setUp() {
        updateRequest = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(SizeFit.LARGE)
                .cushion(Cushion.NORMAL)
                .stability(Stability.STABLE)
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();
                
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        testReview = Review.builder()
                .title("원본 제목")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("원본 내용")
                .user(testUser)
                .build();
        
        // Reflection을 사용하여 reviewId 설정
        try {
            java.lang.reflect.Field reviewIdField = Review.class.getDeclaredField("reviewId");
            reviewIdField.setAccessible(true);
            reviewIdField.set(testReview, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set reviewId", e);
        }
    }

    @Test
    @DisplayName("이미지 없이 리뷰를 성공적으로 수정할 수 있다")
    void updateReview_WithoutImages_Success() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        given(reviewImageService.getActiveImageCount(1L)).willReturn(0);

        // when
        ReviewUpdateResponse response = reviewUpdateService.updateReview(1L, updateRequest, null);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getNewImageUrls()).isEmpty();
        assertThat(response.getDeletedImageIds()).isEmpty();
        assertThat(response.getTotalImageCount()).isEqualTo(0);
        
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
        verify(reviewImageService).getActiveImageCount(1L);
    }

    @Test
    @DisplayName("이미지와 함께 리뷰를 성공적으로 수정할 수 있다")
    void updateReview_WithImages_Success() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        
        List<MultipartFile> newImages = Arrays.asList(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "image1".getBytes())
        );
        
        List<UploadResult> uploadResults = Arrays.asList(
                new UploadResult("image1.jpg", "/uploads/reviews/image1.jpg", "image/jpeg", 1024L)
        );
        
        List<Long> deletedImageIds = Arrays.asList(1L, 2L);
        
        // GCP Storage 서비스가 주입되도록 설정
        reviewUpdateService = new ReviewUpdateService(
                reviewRepository, userRepository, reviewImageService, reviewImageRepository
        );
        
        // Reflection을 사용하여 gcpStorageService 주입
        try {
            java.lang.reflect.Field gcpStorageServiceField = ReviewUpdateService.class.getDeclaredField("gcpStorageService");
            gcpStorageServiceField.setAccessible(true);
            gcpStorageServiceField.set(reviewUpdateService, gcpStorageService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject gcpStorageService", e);
        }
        
        given(reviewImageService.deleteSelectedImages(1L, updateRequest.getDeleteImageIds()))
                .willReturn(deletedImageIds);
        given(gcpStorageService.uploadFilesWithDetails(newImages, UploadDirectory.REVIEWS))
                .willReturn(uploadResults);
        given(reviewImageService.getActiveImageCount(1L)).willReturn(1);

        // when
        ReviewUpdateResponse response = reviewUpdateService.updateReview(1L, updateRequest, newImages);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getNewImageUrls()).hasSize(1);
        assertThat(response.getNewImageUrls()).contains("/uploads/reviews/image1.jpg");
        assertThat(response.getDeletedImageIds()).hasSize(2);
        assertThat(response.getTotalImageCount()).isEqualTo(1);
        
        verify(reviewImageService).deleteSelectedImages(1L, updateRequest.getDeleteImageIds());
        verify(gcpStorageService).uploadFilesWithDetails(newImages, UploadDirectory.REVIEWS);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 수정 시 예외가 발생한다")
    void updateReview_ReviewNotFound_ThrowsException() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewUpdateService.updateReview(999L, updateRequest, null))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("ID 999에 해당하는 리뷰를 찾을 수 없습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 수정 시 예외가 발생한다")
    void updateReview_AccessDenied_ThrowsException() {
        // given
        User otherUser = new User(2L, "otheruser", "password", "other@example.com", UserRole.USER);
        mockSecurityContextForUser(otherUser);
        
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when & then
        assertThatThrownBy(() -> reviewUpdateService.updateReview(1L, updateRequest, null))
                .isInstanceOf(ReviewAccessDeniedException.class)
                .hasMessageContaining("본인이 작성한 리뷰만 수정할 수 있습니다");
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 제목만 수정할 수 있다")
    void updateReviewTitle_Success() {
        // given
        String newTitle = "새로운 제목";
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewUpdateService.updateReviewTitle(1L, newTitle);

        // then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 평점만 수정할 수 있다")
    void updateReviewRating_Success() {
        // given
        Integer newRating = 3;
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewUpdateService.updateReviewRating(1L, newRating);

        // then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 내용만 수정할 수 있다")
    void updateReviewContent_Success() {
        // given
        String newContent = "새로운 내용입니다.";
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewUpdateService.updateReviewContent(1L, newContent);

        // then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("이미지 없이 리뷰를 간단히 수정할 수 있다")
    void updateReviewSimple_Success() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        given(reviewImageService.getActiveImageCount(1L)).willReturn(0);

        // when
        reviewUpdateService.updateReviewSimple(1L, updateRequest);

        // then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 리뷰 수정 시 예외가 발생한다")
    void updateReview_UnauthenticatedUser_ThrowsException() {
        // given
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> reviewUpdateService.updateReview(1L, updateRequest, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("로그인이 필요합니다");
        
        verify(reviewRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 예외가 발생한다")
    void updateReview_ImageUploadFails_ThrowsException() {
        // given
        mockSecurityContext();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        
        List<MultipartFile> newImages = Arrays.asList(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "image1".getBytes())
        );
        
        // GCP Storage 서비스가 주입되도록 설정
        reviewUpdateService = new ReviewUpdateService(
                reviewRepository, userRepository, reviewImageService, reviewImageRepository
        );
        
        // Reflection을 사용하여 gcpStorageService 주입
        try {
            java.lang.reflect.Field gcpStorageServiceField = ReviewUpdateService.class.getDeclaredField("gcpStorageService");
            gcpStorageServiceField.setAccessible(true);
            gcpStorageServiceField.set(reviewUpdateService, gcpStorageService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject gcpStorageService", e);
        }
        
        given(gcpStorageService.uploadFilesWithDetails(newImages, UploadDirectory.REVIEWS))
                .willThrow(new RuntimeException("Storage upload failed"));

        // when & then
        assertThatThrownBy(() -> reviewUpdateService.updateReview(1L, updateRequest, newImages))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미지 업로드에 실패했습니다");
    }

    private void mockSecurityContext() {
        mockSecurityContextForUser(testUser);
    }

    private void mockSecurityContextForUser(User user) {
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn(user.getEmail());
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
    }
}