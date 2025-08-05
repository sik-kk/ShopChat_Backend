package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.service.GcpStorageService;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewUpdateResponse;
import com.cMall.feedShop.review.domain.Review;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 🔍 초보자 설명:
 * 이 테스트는 리뷰 수정 기능이 올바르게 동작하는지 확인합니다.
 * - 권한 검증 (본인이 작성한 리뷰만 수정 가능)
 * - 데이터 검증 (올바른 데이터로 수정되는지)
 * - 예외 상황 처리 (존재하지 않는 리뷰, 권한 없음 등)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 수정 기능 테스트")
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
    private GcpStorageService gcpStorageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewService reviewService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private User testUser;
    private User otherUser;
    private Review testReview;
    private ReviewUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // SecurityContextHolder Mock을 전체 테스트 동안 유지
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);

        // 테스트용 사용자 생성
        testUser = new User("testLogin", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        otherUser = new User("otherLogin", "password", "other@test.com", UserRole.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        // 테스트용 리뷰 생성 (mockito spy 사용으로 실제 메서드 호출 가능)
        testReview = spy(Review.builder()
                .title("원본 제목")
                .rating(4)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.MEDIUM)
                .stability(Stability.STABLE)
                .content("원본 내용입니다.")
                .user(testUser)
                .product(mock())
                .build());
        ReflectionTestUtils.setField(testReview, "reviewId", 1L);

        // 수정 요청 데이터 생성
        updateRequest = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(5)
                .sizeFit(SizeFit.BIG)
                .cushion(Cushion.SOFT)
                .stability(Stability.VERY_STABLE)
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();

        // GCP Storage Service와 ReviewImageRepository 주입
        ReflectionTestUtils.setField(reviewService, "gcpStorageService", gcpStorageService);
        ReflectionTestUtils.setField(reviewService, "reviewImageRepository", reviewImageRepository);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
        }
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인이 작성한 리뷰를 성공적으로 수정할 수 있다")
    void updateReview_Success() {
        // given
        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        given(reviewImageService.deleteSelectedImages(1L, List.of(1L, 2L)))
                .willReturn(List.of(1L, 2L));
        given(reviewImageService.getActiveImageCount(1L)).willReturn(3);

        // when
        ReviewUpdateResponse response = reviewService.updateReview(1L, updateRequest, null);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 수정되었습니다.");
        assertThat(response.getDeletedImageIds()).containsExactly(1L, 2L);
        assertThat(response.getTotalImageCount()).isEqualTo(3);

        // 실제로 리뷰 정보가 수정되었는지 확인
        verify(testReview).updateReviewInfo(
                "수정된 제목",
                5,
                "수정된 내용입니다.",
                SizeFit.BIG,
                Cushion.SOFT,
                Stability.VERY_STABLE
        );
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("새로운 이미지와 함께 리뷰를 수정할 수 있다")
    void updateReview_WithNewImages() {
        // given
        MultipartFile newImage = mock(MultipartFile.class);
        List<MultipartFile> newImages = List.of(newImage);

        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // deleteImageIds Mock 설정
        given(reviewImageService.deleteSelectedImages(eq(1L), eq(List.of(1L, 2L))))
                .willReturn(List.of(1L, 2L));

        // GCP Storage 응답 모킹 - 완전한 Mock 설정
        GcpStorageService.UploadResult uploadResult = mock(GcpStorageService.UploadResult.class);
        given(uploadResult.getFilePath()).willReturn("reviews/new-image.jpg");
        given(uploadResult.getOriginalFilename()).willReturn("new-image.jpg");
        given(uploadResult.getStoredFilename()).willReturn("uuid-new-image.jpg");
        given(uploadResult.getFileSize()).willReturn(1024L);
        given(uploadResult.getContentType()).willReturn("image/jpeg");

        given(gcpStorageService.uploadFilesWithDetails(newImages, "reviews"))
                .willReturn(List.of(uploadResult));

        // ReviewImageRepository.save() Mock - 실제 ReviewImage 타입 반환
        given(reviewImageRepository.save(any())).willAnswer(invocation -> {
            // save 호출 시 전달된 객체를 그대로 반환 (실제 동작과 유사)
            return invocation.getArgument(0);
        });

        given(reviewImageService.getActiveImageCount(1L)).willReturn(4);

        // when
        ReviewUpdateResponse response = reviewService.updateReview(1L, updateRequest, newImages);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getNewImageUrls()).containsExactly("reviews/new-image.jpg");
        assertThat(response.getDeletedImageIds()).containsExactly(1L, 2L);
        assertThat(response.getTotalImageCount()).isEqualTo(4);

        // Mock 호출 검증
        verify(gcpStorageService).uploadFilesWithDetails(newImages, "reviews");
        verify(reviewImageRepository).save(any());
        verify(reviewImageService).deleteSelectedImages(1L, List.of(1L, 2L));
        verify(reviewImageService).getActiveImageCount(1L);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 수정하려 하면 예외가 발생한다")
    void updateReview_ReviewNotFound() {
        // given
        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(999L, updateRequest, null))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("999에 해당하는 리뷰를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 리뷰를 수정하려 하면 권한 예외가 발생한다")
    void updateReview_AccessDenied() {
        // given
        mockSecurityContextForUser(otherUser); // 다른 사용자로 로그인
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(1L, updateRequest, null))
                .isInstanceOf(ReviewAccessDeniedException.class)
                .hasMessageContaining("본인이 작성한 리뷰만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 리뷰를 수정하려 하면 예외가 발생한다")
    void updateReview_Unauthenticated() {
        // given
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        given(securityContext.getAuthentication()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(1L, updateRequest, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("로그인이 필요합니다");
    }

    @Test
    @DisplayName("리뷰 제목만 수정할 수 있다")
    void updateReviewTitle_Success() {
        // given
        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewService.updateReviewTitle(1L, "새로운 제목");

        // then
        verify(testReview).updateTitle("새로운 제목");
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 평점만 수정할 수 있다")
    void updateReviewRating_Success() {
        // given
        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewService.updateReviewRating(1L, 5);

        // then
        verify(testReview).updateRating(5);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 내용만 수정할 수 있다")
    void updateReviewContent_Success() {
        // given
        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);

        // when
        reviewService.updateReviewContent(1L, "새로운 내용");

        // then
        verify(testReview).updateContent("새로운 내용");
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("이미지 업로드 실패 시에도 리뷰 텍스트 수정은 완료된다")
    void updateReview_ImageUploadFailure() {
        // given
        MultipartFile newImage = mock(MultipartFile.class);
        List<MultipartFile> newImages = List.of(newImage);

        mockSecurityContextForUser(testUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        given(reviewImageService.deleteSelectedImages(1L, List.of())).willReturn(List.of());
        given(reviewImageService.getActiveImageCount(1L)).willReturn(0);

        // 이미지 업로드 실패 시뮬레이션
        given(gcpStorageService.uploadFilesWithDetails(newImages, "reviews"))
                .willThrow(new RuntimeException("이미지 업로드 실패"));

        // when
        ReviewUpdateResponse response = reviewService.updateReview(1L, updateRequest, newImages);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getNewImageUrls()).isEmpty(); // 이미지 업로드 실패로 빈 리스트

        // 리뷰 정보는 정상적으로 수정되어야 함
        verify(testReview).updateReviewInfo(any(), any(), any(), any(), any(), any());
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 수정 권한을 확인할 수 있다")
    void canUpdateReview() {
        // given
        given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

        // when
        boolean canUpdate = reviewService.canUpdateReview(1L, testUser.getId());
        boolean cannotUpdate = reviewService.canUpdateReview(1L, otherUser.getId());

        // then
        assertThat(canUpdate).isTrue();
        assertThat(cannotUpdate).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰의 수정 권한 확인 시 false를 반환한다")
    void canUpdateReview_ReviewNotFound() {
        // given
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when
        boolean canUpdate = reviewService.canUpdateReview(999L, testUser.getId());

        // then
        assertThat(canUpdate).isFalse();
    }

    /**
     * SecurityContext 모킹 헬퍼 메서드 - MockedStatic을 전체 테스트에서 공유
     */
    private void mockSecurityContextForUser(User user) {
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getName()).willReturn(user.getEmail());
        given(authentication.getPrincipal()).willReturn(user.getEmail());
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
    }
}