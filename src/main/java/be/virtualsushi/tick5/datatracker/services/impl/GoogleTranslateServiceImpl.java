package be.virtualsushi.tick5.datatracker.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import be.virtualsushi.tick5.datatracker.model.GoogleLanguageDetection;
import be.virtualsushi.tick5.datatracker.model.GoogleLanguageDetectionResult;
import be.virtualsushi.tick5.datatracker.model.GoogleLanguageDetectionResultItem;
import be.virtualsushi.tick5.datatracker.services.GoogleTranslateService;

@Service("googleTranslateService")
public class GoogleTranslateServiceImpl implements GoogleTranslateService {

	private static final Logger log = LoggerFactory.getLogger(GoogleTranslateServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${google.translate.apiKey}")
	private String apiKey;

	@Override
	public String detectLanguage(String... keyWords) {
		String query = "";
		for (String word : keyWords) {
			query += word + "+";
		}
		query = query.substring(0, query.length() - 1);

		try {
			ResponseEntity<GoogleLanguageDetection> response = restTemplate.exchange(GOOGLE_TRANSLATE_URL_PATTERN, HttpMethod.GET, null, GoogleLanguageDetection.class, apiKey, query);
			String language = null;
			GoogleLanguageDetection googleLanguageDetection = response.getBody();
			if (googleLanguageDetection != null) {
				GoogleLanguageDetectionResult googleLanguageDetectionResult = googleLanguageDetection.getRoot();
				if (googleLanguageDetectionResult != null) {
					List<List<GoogleLanguageDetectionResultItem>> itemItems = googleLanguageDetectionResult.getItems();
					if (itemItems != null && !itemItems.isEmpty())
						return getBestLanguage(itemItems.get(0));
				}
			}
		} catch (Exception e) {
			log.debug("COULDN'T DETECT THE LANGUAGE !!!");
		}
		return null;
	}

	private String getBestLanguage(List<GoogleLanguageDetectionResultItem> items) {
		String bestLanguage = null;
		double bestScore = 0;
		if (items != null && (!items.isEmpty())) {
			for (GoogleLanguageDetectionResultItem item : items) {
				if (item.getConfidence() > bestScore) {
					bestScore = item.getConfidence();
					bestLanguage = item.getLanguage();
				}
			}
		}
		if ("af".equals(bestLanguage))
			bestLanguage = "nl";
		return bestLanguage;
	}

}
