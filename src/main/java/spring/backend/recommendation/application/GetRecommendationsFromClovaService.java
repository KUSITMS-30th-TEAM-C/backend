package spring.backend.recommendation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.backend.activity.domain.value.Keyword;
import spring.backend.activity.domain.value.Keyword.Category;
import spring.backend.activity.exception.ActivityErrorCode;
import spring.backend.core.converter.ImageConverter;
import spring.backend.recommendation.presentation.dto.request.AIRecommendationRequest;
import spring.backend.recommendation.presentation.dto.response.ClovaRecommendationResponse;
import spring.backend.recommendation.infrastructure.clova.dto.response.ClovaResponse;
import spring.backend.recommendation.infrastructure.clova.exception.ClovaErrorCode;
import spring.backend.recommendation.infrastructure.map.kakao.dto.response.KakaoMapResponse;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static spring.backend.activity.domain.value.Type.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class GetRecommendationsFromClovaService {
    private static final int MAX_ATTEMPTS = 1;

    private static final Pattern TITLE_FULL_LINE_PATTERN = Pattern.compile(".*title\\s*:.*");
    private static final Pattern TITLE_PREFIX_PATTERN = Pattern.compile(".*title\\s*:");
    private static final Pattern PLACE_NAME_PREFIX_PATTERN = Pattern.compile(".*placeName\\s*:");
    private static final Pattern CONTENT_PREFIX_PATTERN = Pattern.compile(".*content\\s*:");
    private static final Pattern KEYWORD_PREFIX_PATTERN = Pattern.compile(".*keyword\\s*:");
    private static final String LINE_SEPARATOR = "\n";
    private static final int ONLINE_AND_OFFLINE_RECOMMENDATION_COUNT = 3;

    @Value("${kakao.map-uri}")
    private String kakaoMapUri;

    private final RecommendationProvider<ClovaResponse> recommendationProvider;
    private final PlaceInfoProvider<KakaoMapResponse> kakaomapPlaceInfoProvider;
    private final ImageConverter imageConverter;
    private static final Random RANDOM = new Random();

    public List<ClovaRecommendationResponse> getRecommendationsFromClova(AIRecommendationRequest clovaRecommendationRequest) {
        validateLocation(clovaRecommendationRequest);
        List<ClovaRecommendationResponse> clovaResponses = fetchRecommendations(clovaRecommendationRequest);
        int attempt = 1;

        while (containsInvalidKeyword(clovaResponses) && attempt <= MAX_ATTEMPTS) {
            log.warn("추천활동의 키워드가 올바르지 않습니다. 재시도 횟수: {}/{}", attempt, MAX_ATTEMPTS);
            clovaResponses = fetchRecommendations(clovaRecommendationRequest);
            attempt++;
        }

        List<ClovaRecommendationResponse> validRecommendations = filteredValidRecommendations(clovaResponses);

        if (validRecommendations.isEmpty()) {
            throw ClovaErrorCode.INVALID_KEYWORD_IN_RECOMMENDATIONS.toException();
        }

        if (clovaRecommendationRequest.activityType() == ONLINE_AND_OFFLINE) {
            return validRecommendations.stream()
                    .limit(ONLINE_AND_OFFLINE_RECOMMENDATION_COUNT)
                    .collect(Collectors.toList());
        }

        return validRecommendations;

    }

    private List<ClovaRecommendationResponse> filteredValidRecommendations(List<ClovaRecommendationResponse> clovaResponses) {
        return clovaResponses.stream()
                .filter(clovaResponse -> clovaResponse.getKeyword() != null && isValidKeywordCategory(clovaResponse.getKeyword().getCategory())).collect(Collectors.toList());
    }

    private List<ClovaRecommendationResponse> fetchRecommendations(AIRecommendationRequest clovaRecommendationRequest) {
        AIRecommendationRequest filteredClovaRecommendationRequest = filteredValidRecommendations(clovaRecommendationRequest);
        validateClovaRecommendationRequestKeyword(filteredClovaRecommendationRequest);
        ClovaResponse clovaResponse = recommendationProvider.getRecommendations(filteredClovaRecommendationRequest);
        validateClovaResponse(clovaResponse);
        String parsedClovaResponse = clovaResponse.getResult().getMessage().getContent();
        String[] recommendations = parsedClovaResponse.split(LINE_SEPARATOR);

        List<ClovaRecommendationResponse> clovaResponses = new ArrayList<>();
        int order = 1;

        for (int i = 0; i < recommendations.length; i++) {
            String line = recommendations[i].trim();

            if (TITLE_FULL_LINE_PATTERN.matcher(line).matches()) {
                String title = TITLE_PREFIX_PATTERN.matcher(line).replaceFirst("").trim();
                if (i + 1 < recommendations.length && recommendations[i + 1].trim().startsWith("http")) {
                    title += " " + recommendations[i + 1].trim();
                    i++;
                }

                String placeName = "", placeUrl = "", mapx = "", mapy = "";

                if (i + 1 < recommendations.length && PLACE_NAME_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).find()) {
                    placeName = PLACE_NAME_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).replaceFirst("").trim();
                    KakaoMapResponse placeInfo = kakaomapPlaceInfoProvider.search(placeName);

                    if (placeInfo.documents() != null && !placeInfo.documents().isEmpty()) {
                        mapx = placeInfo.documents().get(0).x();
                        mapy = placeInfo.documents().get(0).y();
                        if (Objects.equals(placeInfo.documents().get(0).placeUrl(), "")) {
                            placeUrl = kakaoMapUri;
                        } else {
                            placeUrl = placeInfo.documents().get(0).placeUrl();
                        }
                    }

                    i++;
                }

                String content = "";
                if (i + 1 < recommendations.length && CONTENT_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).find()) {
                    content = CONTENT_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).replaceFirst("").trim();
                    i++;
                }

                Keyword keyword = null;
                if (i + 1 < recommendations.length && KEYWORD_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).find()) {
                    String keywordText = KEYWORD_PREFIX_PATTERN.matcher(recommendations[i + 1].trim()).replaceFirst("").trim();
                    Category category = parsedKeywordTextToCategory(keywordText);
                    keyword = Keyword.create(category, imageConverter.convertToImageUrl(category));
                    i++;
                }
                clovaResponses.add(new ClovaRecommendationResponse(order, title, placeName, mapx, mapy, placeUrl, content, keyword));
                order++;
            }
        }

        return clovaResponses;
    }

    private Category parsedKeywordTextToCategory(String keywordText) {
        if (keywordText == null || keywordText.isEmpty()) {
            return null;
        }

        List<Category> validKeywordCategories = Arrays.stream(keywordText.split(","))
                .map(String::trim)
                .map(this::convertClovaResponseKeywordToKeywordCategory)
                .toList();

        if (validKeywordCategories.isEmpty()) {
            return null;
        }

        RANDOM.setSeed(System.nanoTime());
        int randomIdx = RANDOM.nextInt(validKeywordCategories.size());
        return validKeywordCategories.get(randomIdx);
    }

    private Category convertClovaResponseKeywordToKeywordCategory(String keywordText) {
        if (keywordText == null || keywordText.isEmpty()) {
            return null;
        }
        try {
            return Category.valueOf(keywordText.trim());
        } catch (IllegalArgumentException e) {
            return Arrays.stream(Category.values())
                    .filter(category -> category.getDescription().equals(keywordText))
                    .findFirst()
                    .orElse(null);
        }
    }

    private AIRecommendationRequest filteredValidRecommendations(AIRecommendationRequest clovaRecommendationRequest) {
        if (clovaRecommendationRequest.keywords() == null) {
            return clovaRecommendationRequest;
        }

        Keyword.Category[] filteredKeywords = Arrays.stream(clovaRecommendationRequest.keywords())
                .filter(category -> category != Keyword.Category.SOCIAL)
                .toArray(Keyword.Category[]::new);

        return new AIRecommendationRequest(
                clovaRecommendationRequest.spareTime(),
                clovaRecommendationRequest.activityType(),
                filteredKeywords,
                clovaRecommendationRequest.location()
        );
    }

    private void validateLocation(AIRecommendationRequest clovaRecommendationRequest) {
        if ((clovaRecommendationRequest.activityType() == OFFLINE || clovaRecommendationRequest.activityType() == ONLINE_AND_OFFLINE) &&
                (clovaRecommendationRequest.location() == null || clovaRecommendationRequest.location().isEmpty())) {
            log.error("[AIRecommendationRequest] location must exist when activityType is OFFLINE or ONLINE_AND_OFFLINE");
            throw ActivityErrorCode.NOT_EXIST_LOCATION_WHEN_OFFLINE.toException();
        }

        if (clovaRecommendationRequest.activityType() == ONLINE && clovaRecommendationRequest.location() != null && !clovaRecommendationRequest.location().isEmpty()) {
            log.error("[AIRecommendationRequest] location must not exist when activityType is ONLINE");
            throw ActivityErrorCode.EXIST_LOCATION_WHEN_ONLINE.toException();
        }
    }

    private void validateClovaResponse(ClovaResponse clovaResponse) {
        if (clovaResponse == null || clovaResponse.getResult() == null || clovaResponse.getResult().getMessage() == null || clovaResponse.getResult().getMessage().getContent() == null) {
            log.error("Clova 서비스로부터 null 응답을 수신했습니다.");
            throw ClovaErrorCode.NULL_RESPONSE_FROM_CLOVA.toException();
        }
    }

    private boolean containsInvalidKeyword(List<ClovaRecommendationResponse> clovaResponses) {
        return clovaResponses.stream().anyMatch(clovaResponse ->
                clovaResponse.getKeyword() == null
                        || !isValidKeywordCategory(clovaResponse.getKeyword().getCategory()));
    }

    private boolean isValidKeywordCategory(Category keywordCategory) {
        return Arrays.stream(Category.values()).anyMatch(category -> category == keywordCategory);
    }

    private void validateClovaRecommendationRequestKeyword(AIRecommendationRequest clovaRecommendationRequest) {
        if (clovaRecommendationRequest.activityType().equals(OFFLINE) && Arrays.asList(clovaRecommendationRequest.keywords()).contains(Category.SOCIAL)
        ) {
            throw ClovaErrorCode.OFFLINE_TYPE_CONTAIN_SOCIAL.toException();
        }
    }

}
