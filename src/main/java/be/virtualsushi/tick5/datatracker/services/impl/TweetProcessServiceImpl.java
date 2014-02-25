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

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
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

	@Autowired
	private GoogleTranslateService googleTranslateService;

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private GarbageRepository garbageRepository;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Autowired
	private TweetObjectRepository tweetObjectRepository;

	@Autowired
	private ShortUrlsProcessor shortUrlsProcessor;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${google.translate.apiKey}")
	private String apiKey;

	@Value("${tracking.language}")
	private String trackingLanguage;

	/*
	 * @Value("${garbage.filter}") private String garbageFilter;
	 */

	@Override
	public Tweet processStatus(Status status, boolean saveAfterProcess) {
		if (status.isRetweet()) {
			// Get tweet that was retweeted (which could also be a retweet
			// itself)
			// @todo check if these request do not go over the Twitter limit
			return processStatus(status.getRetweetedStatus(), saveAfterProcess);
		}
		// Check if this tweet is known to the DB
		Tweet tweet = tweetRepository.findOne(status.getId());
		if (tweet == null) {
			tweet = processNewTweet(status);
		}
		if (tweet != null) {
			tweet.setState(TweetStates.NOT_RATED);
			tweet.increaseQuantity(1);
			if (saveAfterProcess) {
				Tweet t = null;
				try {
					t = tweetRepository.save(tweet);
				} catch (Exception e) {
					log.warn("Tweet " + tweet.getId() + " could not be saved");
				}
				;
				return t;
			}
		}
		return tweet;
	}

	private Tweet processNewTweet(Status status) {
		Tweet tweet;
		if (!isGarbage(status)) {
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
					// author.setLanguage(getlanguage(status.getText()));
					// author.setLocation(getLocation(status));
					// if(trackingLanguage.equals(author.getLanguage())){
					author.setListMember(true);
					author.setType(TweepTypes.NEW);
					/*
					 * } else{ author.setListMember(false);
					 * author.setType(TweepTypes.OTHERLANG); }
					 */
					author = twitterUserRepository.save(author);
				} catch (Exception e) {
					log.error("Error fetching twitter user.", e);
				}
			} else {
				author.incrementNumberOfTweets();
				twitterUserRepository.save(author);
			}
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
			tweet.setRecencyFactor(1);
			return tweet;
		}
		return null;
	}

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
		}
		return result;
	}

	private TweetObject processTweetObject(Tweet tweet, String value,
			TweetObjectTypes type) {
		if (TweetObjectTypes.URL.equals(type)) {
			// get original url from shortUrl that was not a t.co url
			value = shortUrlsProcessor.getRealUrl(value);
		}
		TweetObject object = tweetObjectRepository.findByValueAndType(value,
				type);
		if (object == null) {
			object = new TweetObject();
			object.setValue(value);
			object.setType(type);
			object.setTweet(tweet);
		}
		object.increaseQuantity(1);
		return object;
	}

	@Override
	public void deleteTweet(Long tweetId) {
		try {
			tweetRepository.delete(tweetId);
		} catch (Exception e) {
			log.debug("Tweet did NOT get deleted. " + e.getMessage());
		}
	}

	private String getlanguage(String content) {
		String cleaned = removeNoise(content);
		if (canDetectLanguage(cleaned))
			return googleTranslateService.detectLanguage(cleaned.split(" "));
		else
			return null;
	}

	private String getLocation(Status status) {
		String content = (status.getUser().getDescription()
				+ status.getUser().getLocation() + status.getText())
				.toLowerCase();
		if (content.contains(".be") || content.contains("brussel")
				|| content.contains("bruxel") || content.contains("belgie")
				|| content.contains("belgium") || content.contains("belgique")
				|| content.contains("belgië"))
			return "BE";
		if (content.contains(".nl") || content.contains("amsterdam")
				|| content.contains("rotterdam") || content.contains("utrecht")
				|| content.contains("nederland") || content.contains("holland")
				|| content.contains("dutch") || content.contains("netherlands"))
			return "NL";
		else
			return null;
	}

	private boolean canDetectLanguage(String content) {
		return content.split(" ").length > 5 ? true : false;
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

	private boolean isGarbage(Status status) {
		String txt = status.getText();
		if (txt.startsWith("@"))
			return true;
		List<Garbage> words = garbageRepository.findAll();
		for (Garbage word : words) {
			if (txt.toLowerCase().contains(word.getWord()))
				return true;
		}
		return false;
	}

}
