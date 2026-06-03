package com.comne.ejib.domain.property.service;

import com.comne.ejib.domain.property.dto.PropertyDetailResponse;
import com.comne.ejib.domain.property.dto.PropertyImageResponse;
import com.comne.ejib.domain.property.dto.PropertyReviewItemResponse;
import com.comne.ejib.domain.property.dto.PropertyReviewsResponse;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetail(Long propertyId) {
        return propertyRepository.findDetailById(propertyId)
                .map(PropertyDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PropertyImageResponse> getPropertyImages(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        return reviewImageRepository.findAllByPropertyId(propertyId).stream()
                .map(image -> PropertyImageResponse.of(image.getId(), image.getImageUrl()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyReviewsResponse getPropertyReviews(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        List<PropertyReviewItemResponse> reviews = reviewRepository.findReviewItemsByPropertyId(propertyId).stream()
                .map(PropertyReviewItemResponse::from)
                .toList();

        return PropertyReviewsResponse.of(
                reviewRepository.findScoreAveragesByPropertyId(propertyId),
                reviews
        );
    }
}
