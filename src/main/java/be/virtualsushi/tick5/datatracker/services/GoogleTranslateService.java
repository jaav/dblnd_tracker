package be.virtualsushi.tick5.datatracker.services;

import java.util.List;

/**
 * Search for images(for now) with Google's Custom Seach API. Limit: 100
 * query/day. More is for money only.
 * 
 * @author spv
 * 
 */
public interface GoogleTranslateService {

	public static final String GOOGLE_TRANSLATE_URL_PATTERN = "https://www.googleapis.com/language/translate/v2/detect?key={apiKey}&q={query}";

	String getTrackingLanguage(String content);

	boolean isTrackingCountry(String contents);

	String getTrackingLanguageGoogle(String... keyWords);

	String getTrackingLanguageSimple(String... keyWords);

}
