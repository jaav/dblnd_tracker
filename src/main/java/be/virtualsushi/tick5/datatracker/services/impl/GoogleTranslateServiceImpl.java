package be.virtualsushi.tick5.datatracker.services.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.virtualsushi.tick5.datatracker.model.*;
import be.virtualsushi.tick5.datatracker.repositories.CountryMinRepository;
import be.virtualsushi.tick5.datatracker.repositories.CountryPlusRepository;
import be.virtualsushi.tick5.datatracker.repositories.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import be.virtualsushi.tick5.datatracker.services.GoogleTranslateService;

@Service("googleTranslateService")
public class GoogleTranslateServiceImpl implements GoogleTranslateService {

	private static final Logger log = LoggerFactory.getLogger(GoogleTranslateServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LanguageRepository languageRepository;

	@Autowired
	private CountryPlusRepository countryPlusRepository;

	@Autowired
	private CountryMinRepository countryMinRepository;

	/*@Value("${tracking.language.1}")
	private String trackingLanguage1;

	@Value("${tracking.language.2}")
	private String trackingLanguage2;

	@Value("${tracking.language.3}")
	private String trackingLanguage3;*/

	@Value("${google.translate.apiKey}")
	private String apiKey;

	@Override
	public String getTrackingLanguage(String content) {
		String cleaned = removeNoise(content);
		if (canDetectLanguage(cleaned)){
			String discoveredLanguage = getTrackingLanguageSimple(cleaned.split(" "));
			if(discoveredLanguage!=null) return discoveredLanguage;
			else{
				discoveredLanguage = getTrackingLanguageGoogle(cleaned.split(" "));
				if(discoveredLanguage!=null) return discoveredLanguage;
			}
		}
		return null;
	}

	@Override
	public String getTrackingLanguageSimple(String... keyWords) {
		/*if(isTrackingLanguageSimple(trackingLanguage1, keyWords)) return trackingLanguage1;
		if(isTrackingLanguageSimple(trackingLanguage2, keyWords)) return trackingLanguage2;
		if(isTrackingLanguageSimple(trackingLanguage3, keyWords)) return trackingLanguage3;*/
		return null;
	}

	private boolean isTrackingLanguageSimple(String trackingLanguage, String... keyWords){
		List<Language> languageWords = languageRepository.findByLanguage(trackingLanguage);
		int counter = 0;
		for (Language languageWord : languageWords) {
			for (String keyWord : keyWords) {
				if(keyWord.equalsIgnoreCase(languageWord.getWord())){
					counter++;
					break;
				}
			}
			if(counter>=2){
				return true;
			}
		}
		return false;
	}

	@Override
	public String getTrackingLanguageGoogle(String... keyWords){

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
					if (itemItems != null && !itemItems.isEmpty()){
						/*if(getBestLanguage(itemItems.get(0)).equalsIgnoreCase(trackingLanguage1)) return trackingLanguage1;
						if(getBestLanguage(itemItems.get(0)).equalsIgnoreCase(trackingLanguage2)) return trackingLanguage2;
						if(getBestLanguage(itemItems.get(0)).equalsIgnoreCase(trackingLanguage3)) return trackingLanguage3;*/
					}
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

	@Override
	public boolean isTrackingCountry(String content) {
		String cleaned = removeNoise(content);
		String[] keyWords = cleaned.split(" ");
		List<CountryPlus> countryPlusWords = countryPlusRepository.findAll();
		for (CountryPlus countryPlusWord : countryPlusWords) {
			for (String keyWord : keyWords) {
				if(keyWord.equalsIgnoreCase(countryPlusWord.getWord()))
					return true;
			}
		}
		List<CountryMin> countryMinWords = countryMinRepository.findAll();
		for (CountryMin countryMinWord : countryMinWords) {
			for (String keyWord : keyWords) {
				if(keyWord.equalsIgnoreCase(countryMinWord.getWord()))
					return false;
			}
		}
		return true;
	}

	private String removeNoise(String tweet) {
		String cleaned = new String(tweet);
		String pattern = "((http[s]?://[a-zA-Z0-9\\.\\?=&/]*)(?:\\s|$|,|!|\\)))";
		Pattern pt = Pattern.compile(pattern);
		Matcher urlmatcher = pt.matcher(cleaned);
		if (urlmatcher.find()) {
			String regex = urlmatcher.group(0).replace(")", "\\)");
			cleaned = cleaned.replaceAll(regex, "");
		}

		pattern = "([#@]\\w*)(?:\\s|$|,|\\.|\\?|!|\\))";
		pt = Pattern.compile(pattern);
		Matcher tweepmatcher = pt.matcher(cleaned);
		if (tweepmatcher.find()) {
			String regex = tweepmatcher.group(0).replace(")", "\\)");
			cleaned = cleaned.replaceAll(regex, "");
		}
		return cleaned;
	}

	private boolean canDetectLanguage(String content) {
		return content.split(" ").length > 5 ? true : false;
	}

}
