package be.virtualsushi.tick5.datatracker.services.impl;

import java.io.File;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import be.virtualsushi.tick5.datatracker.services.chain.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import be.virtualsushi.tick5.datatracker.components.ImageDownloader;
import be.virtualsushi.tick5.datatracker.components.ImageFilter;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;
import be.virtualsushi.tick5.datatracker.services.ImageProcessService;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

@Service("imageProcessService")
public class ImageProcessServiceImpl implements ImageProcessService {

	private static final Logger log = LoggerFactory.getLogger(ImageProcessServiceImpl.class);

	@Autowired
	private ImageFilter imageFilter;

	@Autowired
	private ImageDownloader imageDownloader;

	@Autowired
	private GoogleSearchService googleSearchService;

	@Autowired
	private Twitter twitter;

	private ProcessChainElement<Tweet, String> processTweetChainRoot;

	//needed to process tweep in case the main process did NOT return an image
	private ProcessChainElement<Tweet, String> backupProcessTweetChainRoot;

	@PostConstruct
	public void initProcessChain() {
		processTweetChainRoot = new ProcessTweetChainImageElement();
		processTweetChainRoot.
				setNext(new ProcessTweetChainUrlElement(imageDownloader)).
				setNext(new ProcessTweetChainTweepElement()).
				setNext(new ProcessTweetChainHashtagElement(googleSearchService)).
				setNext(new ProcessTweetChainTextElement(googleSearchService));
	}

	@PostConstruct
	public void initBackupProcessChain() {
		backupProcessTweetChainRoot = new ProcessTweetChainTweepElement();
		backupProcessTweetChainRoot.
				setNext(new ProcessTweetChainHashtagElement(googleSearchService)).
				setNext(new ProcessTweetChainTextElement(googleSearchService));
	}

	@Async
	@Override
	public Future<File> createTweetImage(Tweet tweet) {
		if(tweet!=null){
			try {
				User user = twitter.showUser(tweet.getUser().getScreenName());
				tweet.getUser().setBackgroundImage(user.getProfileBackgroundImageURL());
				tweet.getUser().setProfileImage(user.getOriginalProfileImageURL());
			} catch (TwitterException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			File result = null;
			try {
				// Look at the @PostConstruct here above to understand chains
				String imageUrl = null;
				imageUrl = processTweetChainRoot.process(tweet);
				if(StringUtils.isBlank(imageUrl))
					imageUrl = backupProcessTweetChainRoot.process(tweet);
				result = imageDownloader.downloadImage(imageUrl);
				// @todo add a check to see if the images are fully downloaded and
				// available or not
			} catch (Exception e) {
				log.error("Error processing image. Tweet id - " + tweet.getId(), e);
			}
			return new AsyncResult<File>(result);
		}
		else return null;
	}

	@Override
	public Future<File> createFilteredImages(String imgName, int filterset) {
		File result = null;
		try {
				imageFilter.applyFancyFilters(imgName, filterset);
		} catch (Exception e) {
			log.error("Error creating fancy filters for " + imgName, e);
		}
		return new AsyncResult<File>(result);
	}
}
