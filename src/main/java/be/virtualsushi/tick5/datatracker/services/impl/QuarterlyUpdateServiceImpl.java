package be.virtualsushi.tick5.datatracker.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.virtualsushi.tick5.datatracker.services.CleanupService;
import be.virtualsushi.tick5.datatracker.services.DataAnalysisService;
import be.virtualsushi.tick5.datatracker.services.QuarterlyUpdateService;

@Service("quarterlyUpdateService")
public class QuarterlyUpdateServiceImpl implements QuarterlyUpdateService {

	private static final Logger log = LoggerFactory.getLogger(QuarterlyUpdateServiceImpl.class);

	@Autowired
	private DataAnalysisService dataAnalysisService;

	@Autowired
	private CleanupService cleanupService;



	/*@Transactional
	@Override
	public void analyseTweets() {
		try {
			List<Tweet> tweets = tweetRepository.getNotRatedTweets();
			log.debug("Rating tweets. Count: " + tweets.size());
			rateTweets(tweets);
			log.debug("Processing top rated tweets.");
			processTopRatedTweets(tweetRepository.getTopRatedTweets(new PageRequest(0, 5, Direction.DESC, "rate")).getContent());
		} catch (Exception e) {
			log.error("Error analysing tweets.", e);
		}
	} */

	@Transactional
	@Override
	public void update() {
	}

}
