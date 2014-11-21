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

	//tweets from 'new' tweeps are accepted if they have a retweeted count bigger then ...
	private static final int NEW_MEMBER_RETWEETS_MINIMUM = 50;


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
				//don't increment maxes here anymore. This done after the analysis.
				//incrementMaxes(status, tweet);
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

	/*private void incrementMaxes(Status status, Tweet tweet){
		if(status.getFavoriteCount()> tweet.getUser().getMaxFavs() || status.getRetweetCount()> tweet.getUser().getMaxRts()){
			TwitterUser author = twitterUserRepository.findOne(tweet.getUser().getId());
			if(status.getFavoriteCount() > tweet.getUser().getMaxFavs()) author.setMaxFavs(status.getFavoriteCount());
			if(status.getRetweetCount() > tweet.getUser().getMaxRts()) author.setMaxRts(status.getRetweetCount());
			twitterUserRepository.save(author);
		}
	}*/

	private Tweet processNewTweet(Status status) {
		TwitterUser author = twitterUserRepository.findOne(status.getUser()
				.getId());
		// If the author of the original tweet is unknown to the DB (most
		// probable), set the auther of the retweet as author of the
		// original tweet
		if (author == null && status.getRetweetCount()>NEW_MEMBER_RETWEETS_MINIMUM) {
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
				author.setMaxFavs(NEW_MEMBER_RETWEETS_MINIMUM);
				author.setMaxRts(NEW_MEMBER_RETWEETS_MINIMUM);
				author = twitterUserRepository.save(author);
			} catch (Exception e) {
				log.error("Error fetching twitter user.", e);
			}
		}
		if(author!=null){
			Tweet tweet = Tweet.fromStatus(status, author);
			// Start processing images, urls and hashtags
			tweet.addObjects(processTweetObjects(tweet, status));
			return tweet;
		}
		else return null;
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
