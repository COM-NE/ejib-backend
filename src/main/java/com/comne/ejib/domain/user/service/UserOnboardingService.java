package com.comne.ejib.domain.user.service;

import com.comne.ejib.domain.user.dto.UserOnboardingRequest;
import com.comne.ejib.domain.user.dto.UserOnboardingResponse;
import com.comne.ejib.domain.user.entity.PreferenceCategory;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.entity.UserPreference;
import com.comne.ejib.domain.user.entity.UserProfile;
import com.comne.ejib.domain.user.entity.UserRequirement;
import com.comne.ejib.domain.user.entity.UserStatus;
import com.comne.ejib.domain.user.repository.PreferenceCategoryRepository;
import com.comne.ejib.domain.user.repository.UserPreferenceRepository;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOnboardingService {

    private static final int REQUIRED_REQUIREMENT_COUNT = 3;

    private final UserRepository userRepository;
    private final PreferenceCategoryRepository preferenceCategoryRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional
    public UserOnboardingResponse complete(Long userId, UserOnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String name = normalizeName(request.name());
        String nickname = normalizeNickname(request.nickname());
        if (userRepository.existsByNicknameAndIdNot(nickname, user.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }

        UserProfile profile = UserProfile.from(request.profile());
        UserStatus status = UserStatus.from(request.status());
        List<String> requirements = normalizeRequirements(request.requirement());
        List<PreferenceCategory> categories = findOrCreateCategories(requirements);

        user.completeOnboarding(name, nickname, profile, status);
        userPreferenceRepository.deleteByUserId(user.getId());
        userPreferenceRepository.saveAll(categories.stream()
                .map(category -> UserPreference.of(user, category))
                .toList());

        return UserOnboardingResponse.completed();
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank() || normalized.length() > 10) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }

    private String normalizeNickname(String nickname) {
        String normalized = nickname == null ? "" : nickname.trim();
        if (normalized.isBlank() || normalized.length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }

    private List<String> normalizeRequirements(List<String> requirementValues) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String requirementValue : requirementValues) {
            normalized.add(UserRequirement.from(requirementValue).value());
        }
        if (normalized.size() != REQUIRED_REQUIREMENT_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_ONBOARDING_REQUIREMENT_COUNT);
        }
        return List.copyOf(normalized);
    }

    private List<PreferenceCategory> findOrCreateCategories(List<String> requirements) {
        List<PreferenceCategory> existingCategories = preferenceCategoryRepository.findAllByNameIn(requirements);
        Map<String, PreferenceCategory> categoryByName = existingCategories.stream()
                .collect(Collectors.toMap(PreferenceCategory::getName, Function.identity()));

        List<PreferenceCategory> categories = new ArrayList<>(requirements.size());
        List<PreferenceCategory> missingCategories = new ArrayList<>();
        for (String requirement : requirements) {
            PreferenceCategory category = categoryByName.get(requirement);
            if (category == null) {
                category = new PreferenceCategory(requirement);
                missingCategories.add(category);
            }
            categories.add(category);
        }

        if (!missingCategories.isEmpty()) {
            preferenceCategoryRepository.saveAll(missingCategories);
        }
        return categories;
    }
}
