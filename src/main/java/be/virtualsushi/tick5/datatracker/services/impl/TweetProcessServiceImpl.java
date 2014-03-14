package be.virtualsushi.tick5.datatracker.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import twitter4j.*;
import be.virtualsushi.tick5.datatracker.components.ShortUrlsProcessor;
import be.virtualsushi.tick5.datatracker.model.Garbage;
import be.virtualsushi.tick5.datatracker.model.TweepTypes;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.model.TweetObject;
import be.virtualsushi.tick5.datatracker.model.TweetObjectTypes;
import be.virtualsushi.tick5.datatracker.model.TweetStates;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import be.virtualsushi.tick5.datatracker.repositories.GarbageRepository;
import be.virtualsushi.tick5.datatracker.repositories.TweetObjectRepository;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;
import be.virtualsushi.tick5.datatracker.repositories.TwitterUserRepository;
import be.virtualsushi.tick5.datatracker.services.GoogleTranslateService;
import be.virtualsushi.tick5.datatracker.services.TweetProcessService;

@Service("tweetProcessService")
public class TweetProcessServiceImpl implements TweetProcessService {

	private static final Logger log = LoggerFactory
			.getLogger(TweetProcessServiceImpl.class);

	//Max age of 2 hours
	private static final long TWEET_MAX_AGE = 7200000;

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Autowired
	private TweetObjectRepository tweetObjectRepository;

	@Autowired
	private ShortUrlsProcessor shortUrlsProcessor;

	@Autowired
	private GoogleTranslateService googleTranslateService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${google.translate.apiKey}")
	private String apiKey;

	/*@Value("${tracking.language.1}")
	private String trackingLanguage1;

	@Value("${tracking.language.2}")
	private String trackingLanguage2;

	@Value("${tracking.language.3}")
	private String trackingLanguage3;

	@Value("${tracking.country}")
	private String trackingCountry;*/

	/*
	 * @Value("${garbage.filter}") private String garbageFilter;
	 */

	@Override
	//@TODO who retweets who?????
	public void processStatus(Status status, boolean saveAfterProcess) {
		if (status.isRetweet()) {
			// Get tweet that was retweeted (which could also be a retweet
			// itself)
			// @todo check if these request do not go over the Twitter limit
			processStatus(status.getRetweetedStatus(), saveAfterProcess);
		}
		if(System.currentTimeMillis() - status.getCreatedAt().getTime() < TWEET_MAX_AGE){
			// Check if this tweet is known to the DB
			Tweet tweet = tweetRepository.findOne(status.getId());
			if (tweet == null)
				tweet = processNewTweet(status);
			if (tweet != null) {
				tweet.setState(TweetStates.NOT_RATED);
				tweet.setRetweets(status.getRetweetCount());
				tweet.setFavorites(status.getFavoriteCount());
				incrementMaxes(status, tweet);
				if (saveAfterProcess) {
					try {
						tweetRepository.save(tweet);
					} catch (Exception e) {
						log.warn("Tweet could not be saved: {},,, id = {}", tweet.getText(), tweet.getId());
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void incrementMaxes(Status status, Tweet tweet){
		if(status.getFavoriteCount()> tweet.getUser().getMaxFavs() || status.getRetweetCount()> tweet.getUser().getMaxRts()){
			TwitterUser author = twitterUserRepository.findOne(tweet.getUser().getId());
			if(status.getFavoriteCount() > tweet.getUser().getMaxFavs()) author.setMaxFavs(status.getFavoriteCount());
			if(status.getRetweetCount() > tweet.getUser().getMaxRts()) author.setMaxRts(status.getRetweetCount());
			twitterUserRepository.save(author);
		}
	}

	private Tweet processNewTweet(Status status) {
		Tweet tweet;
		TwitterUser author = twitterUserRepository.findOne(status.getUser()
				.getId());
		// If the author of the original tweet is unknown to the DB (most
		// probable), set the auther of the retweet as author of the
		// original tweet
		if (author == null) {
			try {
				// author =
				// TwitterUser.fromUser(twitter.showUser(status.getUser().getId()));
				author = new TwitterUser();
				author.setId(status.getUser().getId());
				author.setName(status.getUser().getName());
				author.setScreenName(status.getUser().getScreenName());

//					author.setLanguage(getlanguage(status.getText()));
//					author.setLocation(getLocation(status));
				// if(trackingLanguage.equals(author.getLanguage())){
				author.setListMember(true);
				author.setType(TweepTypes.NEW);
				author.setMaxFavs(status.getFavoriteCount()>0 ? status.getFavoriteCount() : 1);
				author.setMaxRts(status.getRetweetCount()>0 ? status.getRetweetCount() : 1);
					/*
					 * } else{ author.setListMember(false);
					 * author.setType(TweepTypes.OTHERLANG); }
					 */
			} catch (Exception e) {
				log.error("Error fetching twitter user.", e);
			}
		}
		/*if (author.getLanguage() == null) {
			String discoveredLanguage = getTrackingLanguage(status.getText());
			if (discoveredLanguage != null) {
				log.debug("Language of tweet = {}", discoveredLanguage);
				author.setLanguage(discoveredLanguage);
			}
		}*/
		/*if (author.getLocation() == null) {
			if (containsBelgium(status.getUser().getLocation(), status.getUser().getURL()))
				author.setLocation(trackingCountry);
			else if (isBelgium(status.getGeoLocation()))
				author.setLocation(trackingCountry);
			else if (containsHolland(status.getUser().getLocation(), status.getUser().getURL())) {
				author.setLocation("NL");
				author.setType(TweepTypes.OTHER);
			} else if (isHolland(status.getGeoLocation())) {
				author.setLocation("NL");
				author.setType(TweepTypes.OTHER);
			} else if (containsFrance(status.getUser().getLocation(), status.getUser().getURL())) {
				author.setLocation("FR");
				author.setType(TweepTypes.OTHER);
			} else if (isFrance(status.getGeoLocation())) {
				author.setLocation("FR");
				author.setType(TweepTypes.OTHER);
			}
		}*/
		else{
			if(status.getFavoriteCount() > author.getMaxFavs()) author.setMaxFavs(status.getFavoriteCount());
			if(status.getRetweetCount() > author.getMaxRts()) author.setMaxRts(status.getRetweetCount());
		}
		author = twitterUserRepository.save(author);
			/*
			 * else if(author.getLanguage()==null){ try {
			 * author.setLanguage(getlanguage(status.getText()));
			 * if(author.getLanguage()!=null){
			 * if(trackingLanguage.equals(author.getLanguage())){
			 * author.setListMember(true); author.setType(TweepTypes.MEMBER); }
			 * else{ author.setListMember(false);
			 * author.setType(TweepTypes.OTHERLANG); } author =
			 * twitterUserRepository.save(author); } } catch (Exception e) {
			 * log.error("Error fetching twitter user.", e); } }
			 */
		tweet = Tweet.fromStatus(status, author);
		// Start processing images, urls and hashtags
		tweet.addObjects(processTweetObjects(tweet, status));
		return tweet;
	}

	/*private boolean containsBelgium(String location, String url) {
		if (url != null && url.indexOf(".be") >= 0) return true;
		String[] belgianPointers = {"Belgie", "Belgium", "Brussel", "Antwerp", "Gent", "Ghent", "Leuven", "ostend", "Brugge",
				"Kortrijk", "Aalst", "Sint Niklaas", "Mechelen", "Roeselare", "Izegem", "Turnhout", "Genk", "Hasselt"};
		for (int i = 0; i < belgianPointers.length; i++)
			if (location.toLowerCase().indexOf(belgianPointers[i].toLowerCase()) >= 0) return true;
		return false;
	}

	private boolean isBelgium(GeoLocation geoLoc) {
		if (geoLoc == null) return false;
		GeoLocation[] geos = new GeoLocation[20];
		int i = 0;
		geos[i++] = new GeoLocation(51.378638, 2.504883);
		geos[i++] = new GeoLocation(50.726024, 3.586178);
		geos[i++] = new GeoLocation(51.303145, 3.509274);
		geos[i++] = new GeoLocation(50.464498, 5.821896);
		geos[i++] = new GeoLocation(51.474540, 4.162960);
		geos[i++] = new GeoLocation(51.316881, 5.140743);
		geos[i++] = new GeoLocation(50.795519, 5.761471);
		geos[i++] = new GeoLocation(50.443513, 6.365719);
		geos[i++] = new GeoLocation(50.771208, 3.163204);
		geos[i++] = new GeoLocation(50.495958, 3.564205);
		geos[i++] = new GeoLocation(50.429518, 3.668575);
		geos[i++] = new GeoLocation(50.310392, 4.130001);
		geos[i++] = new GeoLocation(50.443513, 4.168453);
		geos[i++] = new GeoLocation(49.958288, 4.695797);
		geos[i++] = new GeoLocation(50.447011, 4.690304);
		geos[i++] = new GeoLocation(50.190968, 6.382198);
		geos[i++] = new GeoLocation(50.159305, 4.849606);
		geos[i++] = new GeoLocation(49.841525, 5.794430);
		geos[i++] = new GeoLocation(49.784811, 5.173702);
		geos[i++] = new GeoLocation(49.539469, 5.849361);

		for (int j = 0; j < geos.length; j++) {
			if (geos[j].getLatitude() > geoLoc.getLatitude()
					&& geos[j].getLongitude() < geoLoc.getLongitude()
					&& geos[j + 1].getLatitude() > geoLoc.getLatitude()
					&& geos[j + 1].getLongitude() < geoLoc.getLongitude()
					) {
				log.debug("Belgian geoloc found! {} lies between {} and {} and {} lies between {} and {}", geoLoc.getLongitude(), geos[j].getLongitude(), geos[j + 1].getLongitude(), geoLoc.getLatitude(), geos[j].getLatitude(), geos[j + 1].getLatitude());
				return true;
			}
			j += 2;
		}
		return false;
	}

	private boolean isHolland(GeoLocation geoLoc) {
		if (geoLoc == null) return false;
		GeoLocation upperLeft = new GeoLocation(53.579461, 2.953606);
		GeoLocation lowerRight = new GeoLocation(50.771208, 7.24926);
		if (upperLeft.getLatitude() > geoLoc.getLatitude()
				&& upperLeft.getLongitude() < geoLoc.getLongitude()
				&& lowerRight.getLatitude() > geoLoc.getLatitude()
				&& lowerRight.getLongitude() < geoLoc.getLongitude()
				) {
			log.debug("Dutch geoloc found! {} lies between {} and {} and {} lies between {} and {}", geoLoc.getLongitude(), upperLeft.getLongitude(), lowerRight.getLongitude(), geoLoc.getLatitude(), upperLeft.getLatitude(), lowerRight.getLatitude());
			return true;
		}
		return false;
	}

	private boolean isFrance(GeoLocation geoLoc) {
		if (geoLoc == null) return false;
		GeoLocation upperLeft = new GeoLocation(50.583237, -4.661636);
		GeoLocation lowerRight = new GeoLocation(42.081917, 9.049301);
		if (upperLeft.getLatitude() > geoLoc.getLatitude()
				&& upperLeft.getLongitude() < geoLoc.getLongitude()
				&& lowerRight.getLatitude() > geoLoc.getLatitude()
				&& lowerRight.getLongitude() < geoLoc.getLongitude()
				) {
			log.debug("French geoloc found! {} lies between {} and {} and {} lies between {} and {}", geoLoc.getLongitude(), upperLeft.getLongitude(), lowerRight.getLongitude(), geoLoc.getLatitude(), upperLeft.getLatitude(), lowerRight.getLatitude());
			return true;
		}
		return false;
	}

	private boolean containsHolland(String location, String url) {
		if (url != null && url.indexOf(".nl") >= 0) return true;
		if (url != null && url.indexOf(".fr") >= 0) return true;
		String[] belgianPointers = {"Nederland", "Netherland", "Amsterdam", "Rotterdam", "Den Haag", "Utrecht", "Groningen", "Eindhoven", "Nijmegen",
				"Breda"};
		for (int i = 0; i < belgianPointers.length; i++)
			if (location.toLowerCase().indexOf(belgianPointers[i].toLowerCase()) >= 0) return true;
		return false;
	}

	private boolean containsFrance(String location, String url) {
		String[] belgianPointers = {"France", "Paris", "Nantes", "Bordeaux", "Marseille", "Lille", "Le Havre", "Caen", "Reims",
				"Nancy", "Toulouse", "Brest", "Strasbourg", "Dijon", "Lyon", "Grenoble", "Avignon", "Versailles", "Orleans"};
		for (int i = 0; i < belgianPointers.length; i++)
			if (location.toLowerCase().indexOf(belgianPointers[i].toLowerCase()) >= 0) return true;
		return false;
	}*/

	private List<TweetObject> processTweetObjects(Tweet tweet, Status status) {
		List<TweetObject> result = new ArrayList<TweetObject>();
		for (HashtagEntity hashtag : status.getHashtagEntities()) {
			result.add(processTweetObject(tweet, hashtag.getText(),
					TweetObjectTypes.HASHTAG));
		}
		for (URLEntity url : status.getURLEntities()) {
			// Set url or original url if the shortUrl is of type t.co
			// (=getExpandedURL functionality)
			result.add(processTweetObject(tweet, url.getExpandedURL(),
					TweetObjectTypes.URL));
		}
		for (MediaEntity media : status.getMediaEntities()) {
			// Get all media files from this tweet
			// @todo check if really ALL images are being fetched when using
			// this method
			result.add(processTweetObject(tweet, media.getMediaURL(),
					TweetObjectTypes.IMAGE));

			//adding the url from the media to activate it in the front-end

			List<TweetObject> object = tweetObjectRepository.findByValueAndType(media.getMediaURL(), TweetObjectTypes.URL);
			TweetObject tweetObject = null;
			if (object == null || object.isEmpty()) {
				tweetObject = new TweetObject();
				tweetObject.setValue(media.getMediaURL());
				tweetObject.setType(TweetObjectTypes.URL);
				tweetObject.setTweet(tweet);
			}
			else
				tweetObject = object.get(0);

			tweetObject.increaseQuantity(1);
			result.add(tweetObject);
		}
		return result;
	}

	private TweetObject processTweetObject(Tweet tweet, String value,
	                                       TweetObjectTypes type) {
		if (TweetObjectTypes.URL.equals(type)) {
			// get original url from shortUrl that was not a t.co url
			value = shortUrlsProcessor.getRealUrl(value);
		}
		List<TweetObject> object = tweetObjectRepository.findByValueAndType(value, type);
		TweetObject tweetObject = null;
		if (object == null || object.isEmpty()) {
			tweetObject = new TweetObject();
			tweetObject.setValue(value);
			tweetObject.setType(type);
			tweetObject.setTweet(tweet);
		}
		else
			tweetObject = object.get(0);
		tweetObject.increaseQuantity(1);
		return tweetObject;
	}

	@Override
	public void deleteTweet(Long tweetId) {
		try {
			tweetRepository.delete(tweetId);
		} catch (Exception e) {
			log.debug("Tweet did NOT get deleted. " + e.getMessage());
		}
	}


	/*private String getTrackingLanguage(String content) {
		return googleTranslateService.getTrackingLanguage(content);
	}


	private boolean isTrackingCountry(String content) {
		return googleTranslateService.isTrackingCountry(content);
	}*/

}
