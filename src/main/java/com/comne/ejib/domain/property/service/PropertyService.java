package com.comne.ejib.domain.property.service;

import com.comne.ejib.domain.property.dto.PropertyDetailResponse;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetail(Long propertyId) {
        return propertyRepository.findDetailById(propertyId)
                .map(PropertyDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPERTY_NOT_FOUND));
    }
}
