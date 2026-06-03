package com.comne.ejib.domain.property.service;

import com.comne.ejib.domain.property.dto.PropertyImageResponse;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyImageService {

    private final PropertyRepository propertyRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Transactional(readOnly = true)
    public List<PropertyImageResponse> getPropertyImages(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        return reviewImageRepository.findAllByPropertyId(propertyId).stream()
                .map(image -> PropertyImageResponse.of(image.getId(), image.getImageUrl()))
                .toList();
    }
}
