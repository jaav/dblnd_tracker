package be.virtualsushi.tick5.datatracker.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.virtualsushi.tick5.datatracker.model.*;
import be.virtualsushi.tick5.datatracker.repositories.*;
import be.virtualsushi.tick5.datatracker.services.GoogleTranslateService;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import twitter4j.Status;
import twitter4j.Twitter;
import be.virtualsushi.tick5.datatracker.services.DataAnalysisService;
import be.virtualsushi.tick5.datatracker.services.ImageProcessService;

@Service("dataAnalysisService")
public class DataAnalysisServiceImpl implements DataAnalysisService {

	//private static final float BESTSELLER_LIMIT_FACTOR = 0.2f;

	private static final int MINIMUM_FAVS = 3;

	private static final int MINIMUM_RETWEETS = 3;

	private static final int RECENCY_DIVIDER = 1;

	private static final int PUBLISHED_TWICE_DIVIDER = 5;

	private static final int POPULARSOURCE_DIVIDER = 4;

	private static final int SUBSIDIZED_URL_MULTIPLICATOR = 4;

	private static final int SUBSIDIZED_CONTENT_MULTIPLICATOR = 3;

	private static final int GARBAGE_DIVIDER = 10;

	private static final int LEADER_MINIMUM_RETWEETS = 2500;

	private static final int LEADER_MINIMUM_FAVORITES = 2500;

	private static final Logger log = LoggerFactory.getLogger(DataAnalysisServiceImpl.class);

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private SelfTweetRepository selfTweetRepository;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Autowired
	private ImageProcessService imageProcessService;

	@Autowired
	private AwsRepository awsRepository;

	@Autowired
	private Twitter twitter;

	@Autowired
	private PopularSourceRepository popularSourceRepository;

	@Autowired
	private SubsidizedRepository subsidizedRepository;

	@Autowired
	private SubsidizedUrlRepository subsidizedUrlRepository;

	@Autowired
	private GarbageRepository garbageRepository;

	@Autowired
	private GoogleTranslateService googleTranslateService;

	/*@Value("${tracking.country}")
	private String trackingCountry;

	@Value("${tracking.language.1}")
	private String trackingLanguage1;

	@Value("${tracking.language.2}")
	private String trackingLanguage2;

	@Value("${tracking.language.3}")
	private String trackingLanguage3;*/

	@Value("${twitter.publish}")
	private Boolean twitterPublish;

	@Override
	public void analyseTweets(String aws_key) {
		try {
			List<Tweet> tweets = tweetRepository.getNotRatedTweets();
			log.debug("Found {} not-rated tweets", tweets.size());
			// Rating tweets.
			rateTweets(tweets);
			// Processing top rated tweets.
			processTopRatedTweets(tweetRepository.getTopRatedTweets(new PageRequest(0, 5, Direction.DESC, "rate")).getContent(), aws_key);
			log.warn("Finished analysis");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error analysing tweets.", e);
		}
	}

	private void processTopRatedTweets(List<Tweet> topRatedTweets, String aws_key) throws InterruptedException, ExecutionException, IOException {
		log.debug("Top rating a list of {} tweets", topRatedTweets.size());
		/*List<Tweet> popularTweets = createPopularTweetsList(topRatedTweets);
		log.debug("Publishing a list of {} tweets", popularTweets.size());*/
		Map<Long, Future<File>> futureImageFiles = new HashMap<Long, Future<File>>();
		Map<Long, Tweet> tweetsToPost = new HashMap<Long, Tweet>();
		long id = System.currentTimeMillis();
		for (Tweet tweet : topRatedTweets) {
			log.debug("Setting {} to 'published'", tweet.getId());
			tweetRepository.setPublished(tweet.getId());
			log.debug("Ok, tweet was set 'published'");
			Tweet topper = new Tweet();
			// topper.setLocation(tweet.getLocation());
			topper.setRetweets(tweet.getRetweets());
			topper.setRate(tweet.getRate());
			topper.setText(tweet.getText());
			topper.setUser(tweet.getUser());
			topper.setTweep(tweet.getTweep());
			topper.setId(id++);
			topper.setState(TweetStates.TOP_RATED);
			topper.setObjects(tweet.getObjects());
			topper.setRetweeted(false);
			topper.setPublished(false);
			topper.setFavorites(tweet.getFavorites());
			if (StringUtils.isBlank(tweet.getImage())) {
				futureImageFiles.put(tweet.getId(), imageProcessService.createTweetImage(topper));
			}
			tweetsToPost.put(tweet.getId(), topper);
		}
		for (Long aLong : futureImageFiles.keySet()) {
			while (!futureImageFiles.get(aLong).isDone()) {
				log.debug("Image for " + aLong + " is not ready yet.");
				Thread.sleep(100);
			}
			File img = futureImageFiles.get(aLong).get();
			if (img != null)
				log.debug(futureImageFiles.get(aLong).get().getAbsolutePath() + futureImageFiles.get(aLong).get().getName());
		}
		for (Long aLong : futureImageFiles.keySet()) {
			if (futureImageFiles.containsKey(aLong)) {
				File imageFile = futureImageFiles.get(aLong).get();
				if (imageFile != null)
					tweetsToPost.get(aLong).setImage(imageFile.getName().substring(0, imageFile.getName().lastIndexOf(".")));
			}
			try {
				List<TweetObject> cache = tweetsToPost.get(aLong).getObjects();
				tweetsToPost.get(aLong).setObjects(null);
				tweetRepository.save(tweetsToPost.get(aLong));
				tweetsToPost.get(aLong).setObjects(cache);
			} catch (Exception e) {
				log.warn("Tweet " + tweetsToPost.get(aLong).getId() + " could NOT be saved - " + e.getMessage());
				e.printStackTrace();
			}

			// Set this tweep as opinion leader
			if (tweetsToPost.get(aLong).getRetweets() > LEADER_MINIMUM_RETWEETS || tweetsToPost.get(aLong).getFavorites() > LEADER_MINIMUM_FAVORITES) {
				TwitterUser dbUser = tweetsToPost.get(aLong).getUser();
				dbUser.setType(TweepTypes.LEADER);
				twitterUserRepository.save(dbUser);
			}
		}
		awsRepository.insertTweets(tweetsToPost, aws_key);
	}

	public void retweet(){
		List<Tweet> tweets = tweetRepository.getTopRatedTweetsForRetweeting();
		//tweets = createPopularTweetsList(tweets);
		int counter = 0;
		for (Tweet tweet : tweets) {
			if(counter<5){
				try {
					SelfTweet selfTweet = selfTweetRepository.findOne(tweet.getId());
					if (selfTweet == null) {
						if (twitterPublish) {
							twitter.retweetStatus(tweet.getId());
							log.debug("RETWEETING TWEET {}", tweet.getId());
						}
						tweetRepository.setRetweeted(tweet.getId());
						selfTweet = SelfTweet.fromTweet(tweet);
						selfTweetRepository.save(selfTweet);
						}
				} catch (Exception e) {
					e.printStackTrace();
				}
				counter++;
			}
		}
	}

	/*private List<Tweet> createPopularTweetsList(List<Tweet> topRatedTweets) {
		List<Tweet> popularTweets = new ArrayList<Tweet>();
		int counter = 0;
		while (popularTweets.size() < 5 && topRatedTweets.size() > counter) {
			Tweet tweet = topRatedTweets.get(counter++);
			log.debug("What's the Tweeptype? {}", TweepTypes.MEMBER.name());
			log.debug("What's the user's tweeptype? {}", tweet.getUser().getType());
			if (TweepTypes.MEMBER.equals(tweet.getUser().getType()))
				popularTweets.add(tweet);
		}
		return popularTweets;
	}*/

	private void rateTweets(List<Tweet> tweets) {
		for (Tweet tweet : tweets) {
			try {
				double quantityQuote = tweet.getRetweets() > MINIMUM_RETWEETS ? 4 * tweet.getRetweets() / ((double) tweet.getUser().getMaxRts()) : 0;
				log.debug("quantityQuote = {}", quantityQuote);

				double favsQuote = tweet.getFavorites() > MINIMUM_FAVS ? 4 * tweet.getFavorites() / ((double) tweet.getUser().getMaxFavs()) : 0;
				log.debug("favsQuote = {}", favsQuote);

				int subsedizedUrlMultiplicator = 1;
				for (TweetObject object : tweet.getObjects()) {
					if (object.getType() == TweetObjectTypes.URL && isSubsidizedUrl(object.getValue()))
						subsedizedUrlMultiplicator = SUBSIDIZED_URL_MULTIPLICATOR;
				}
				log.debug("subsedizedUrlMultiplicator = {}", subsedizedUrlMultiplicator);

				int subsidizedContentMultiplicator = isSubsidizedTweet(tweet.getText()) ? SUBSIDIZED_CONTENT_MULTIPLICATOR : 1;
				log.debug("subsidizedContentMultiplicator = {}", subsidizedContentMultiplicator);

				int popularSourceDivider = isPopularSource(tweet.getUser().getScreenName()) ? POPULARSOURCE_DIVIDER : 1;
				log.debug("popularSourceDivider = {}", popularSourceDivider);

				int garbageDivider = isGarbage(tweet.getText()) ? GARBAGE_DIVIDER : 1;
				log.debug("garbageDivider = {}", garbageDivider);

				int publishedTwiceDivider = tweet.getPublished() ? PUBLISHED_TWICE_DIVIDER : 1;
				log.debug("publishedTwiceDivider = {}", publishedTwiceDivider);

				/**************** ACTUAL RATE CALCULUS *************************/
				int rate = (int) (100 * (quantityQuote + favsQuote)
						* subsedizedUrlMultiplicator
						* subsidizedContentMultiplicator
						/ popularSourceDivider
						/ garbageDivider
						/ publishedTwiceDivider);
				tweet.setRate(rate);
				log.debug("tweet.getRate = {}", tweet.getRate());

				tweet.setRecencyFactor(tweet.getRecencyFactor() / RECENCY_DIVIDER);
				tweet.setState(TweetStates.RATED);
				tweetRepository.rateTweet(tweet.getId(), tweet.getRate(), TweetStates.RATED.name(), tweet.getRecencyFactor());

				if(tweet.getFavorites() > tweet.getUser().getMaxFavs() || tweet.getRetweets() > tweet.getUser().getMaxRts()){
					tweet.getUser().setMaxFavs(tweet.getFavorites());
					tweet.getUser().setMaxRts(tweet.getRetweets());
					twitterUserRepository.save(tweet.getUser());
				}

			} catch (Exception e) {
				log.warn("Not rating tweet " + tweet.getId());
				e.printStackTrace();
			}
		}
	}

	private boolean isPopularSource(String screeName) {
		List<PopularSource> sources = popularSourceRepository.findAll();
		for (PopularSource source : sources) {
			if (screeName.equals(source.getScreenName()))
				return true;
		}
		return false;
	}

	private boolean isSubsidizedTweet(String text) {
		List<Subsidized> subsidizedList = subsidizedRepository.findAll();
		for (Subsidized subsidized : subsidizedList) {
			if (text.toLowerCase().contains(subsidized.getWord()))
				return true;
		}
		return false;
	}

	private boolean isSubsidizedUrl(String url) {
		List<SubsidizedUrl> subsidizedUrlList = subsidizedUrlRepository.findAll();
		for (SubsidizedUrl subsidizedUrl : subsidizedUrlList) {
			if (url.toLowerCase().contains(subsidizedUrl.getWord()))
				return true;
		}
		return false;
	}


	private boolean isGarbage(String txt) {
		List<Garbage> words = garbageRepository.findAll();
		for (Garbage word : words) {
			if (txt.toLowerCase().contains(word.getWord()))
				return true;
		}
		return false;
	}


	/*private String getTrackingLanguage(String content) {
		return googleTranslateService.getTrackingLanguage(content);
	}


	private boolean isTrackingCountry(String content) {
			return googleTranslateService.isTrackingCountry(content);
	}*/

}
