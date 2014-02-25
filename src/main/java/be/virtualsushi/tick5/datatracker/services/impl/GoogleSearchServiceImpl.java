package be.virtualsushi.tick5.datatracker.services.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import be.virtualsushi.tick5.datatracker.model.GoogleImageSearchResult;
import be.virtualsushi.tick5.datatracker.model.GoogleSearchCachedResult;
import be.virtualsushi.tick5.datatracker.repositories.GoogleSearchCachedResultRepository;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;

@Service("googleSearchService")
public class GoogleSearchServiceImpl implements GoogleSearchService {

	private static final Logger log = LoggerFactory.getLogger(GoogleSearchServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${google.search.apiKey}")
	private String apiKey;

	@Value("${google.search.engineId}")
	private String engineId;

	@Value("${google.search.priorityOnCache}")
	private boolean priorityOnCache;

	@Autowired
	private GoogleSearchCachedResultRepository googleSearchCachedResultRepository;

	@Override
	public String searchForImage(String... keyWords) {
		String query = "";
		for (String word : keyWords) {
			query += word + "+";
		}
		query = query.substring(0, query.length() - 1);
		log.debug("Searching for images about: "+query);
		if (priorityOnCache) {
			List<GoogleSearchCachedResult> result = googleSearchCachedResultRepository.findByQuery(query);
			if (CollectionUtils.isNotEmpty(result)) {
				return result.get(0).getResultLink();
			}
			for (String word : keyWords) {
				result = googleSearchCachedResultRepository.findByQuery("%" + word + "%");
				if (CollectionUtils.isNotEmpty(result)) {
					return result.get(0).getResultLink();
				}
			}
		}
		ResponseEntity<GoogleImageSearchResult> response = restTemplate.exchange(GOOGLE_IMAGE_SEARCH_URL_PATTERN, HttpMethod.GET, null, GoogleImageSearchResult.class, apiKey, engineId, query);
		String resultLink = null;
		try{
			resultLink = response.getBody().getItems().get(0).getLink();
			GoogleSearchCachedResult cachedResult = new GoogleSearchCachedResult();
			cachedResult.setQueryString(query);
			cachedResult.setResultLink(resultLink);
			googleSearchCachedResultRepository.save(cachedResult);
		}
		catch(Exception e){
			log.debug("DIDN'T FIND A GOOGLE IMAGE !!!");
			return searchForSmallerImage(keyWords);
		}
		return resultLink;
	}

	@Override
	public String searchForImage(List<String> keyWords) {
		return searchForImage(keyWords.toArray(new String[keyWords.size()]));
	}

	@Override
	public String searchForImage(String keyPhrase) {
		return searchForImage(keyPhrase.split(" "));
	}



	private String searchForSmallerImage(String... keyWords) {
		String query = "";
		for (String word : keyWords) {
			query += word + "+";
		}
		query = query.substring(0, query.length() - 1);
		ResponseEntity<GoogleImageSearchResult> response = restTemplate.exchange(GOOGLE_SMALLER_IMAGE_SEARCH_URL_PATTERN, HttpMethod.GET, null, GoogleImageSearchResult.class, apiKey, engineId, query);
		String resultLink = null;
		try{
			resultLink = response.getBody().getItems().get(0).getLink();
			GoogleSearchCachedResult cachedResult = new GoogleSearchCachedResult();
			cachedResult.setQueryString(query);
			cachedResult.setResultLink(resultLink);
			googleSearchCachedResultRepository.save(cachedResult);
		}
		catch(Exception e){

			log.debug("DIDN'T FIND A SMALLER GOOGLE IMAGE EITHER for "+query+" !!!");
		}
		return resultLink;
	}

}
