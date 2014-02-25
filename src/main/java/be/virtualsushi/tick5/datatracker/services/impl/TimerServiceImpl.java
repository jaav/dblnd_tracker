package be.virtualsushi.tick5.datatracker.services.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import be.virtualsushi.tick5.datatracker.components.ImageFilter;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;
import be.virtualsushi.tick5.datatracker.services.CleanupService;
import be.virtualsushi.tick5.datatracker.services.DataAnalysisService;
import be.virtualsushi.tick5.datatracker.services.DataCreationService;
import be.virtualsushi.tick5.datatracker.services.PublishService;
import be.virtualsushi.tick5.datatracker.services.TimerService;

@Service("timerService")
public class TimerServiceImpl implements TimerService {

	private static final Logger log = LoggerFactory.getLogger(TimerServiceImpl.class);

	final int filterset = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET")).get(Calendar.MINUTE) / 15 % 2 == 0 ? 1 : 2;

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private CleanupService cleanupService;

	@Autowired
	private ImageFilter imageFilter;

	@Autowired
	private DataAnalysisService dataAnalysisService;

	@Autowired
	private PublishService publishService;

	@Autowired
	private DataCreationService dataCreationService;

	@Override
	@Scheduled(cron = "0 10,25,40,55 * * * *")
	public void clean() {
		cleanupService.clean(createAWSKey(-4));
	}

	@Override
	@Scheduled(cron = "0 11,26,41,56 * * * *")
	public void analyse() {
		String aws_key = createAWSKey(0);
		dataAnalysisService.analyseTweets(aws_key);
	}

	@Override
	@Scheduled(cron = "0 12,27,42,57 * * * *")
	public void createSquaredImages() {
		List<Tweet> bestsellerTweets = tweetRepository.getToppers();
		for (Tweet bestsellerTweet : bestsellerTweets) {
			imageFilter.applySquareFilter(bestsellerTweet.getImage());
		}
	}

	@Override
	@Scheduled(cron = "0 13,28,43,58 * * * *")
	public void createFancyImages() {
		List<Tweet> bestsellerTweets = tweetRepository.getToppers();
		for (Tweet bestsellerTweet : bestsellerTweets) {
			imageFilter.applyFancyFilters(bestsellerTweet.getImage(), filterset);
		}
	}

	@Override
	@Scheduled(cron = "45 14,29,44,59 * * * *")
	public void publish() {
		String aws_key = createAWSKey(0);
		publishService.publish(aws_key);
	}

	@Override
	// run restart every day at 2 past 4AM
	@Scheduled(cron = "0 2 4 * * *")
	public void relaunch() {
		dataCreationService.restartDataTracking();
	}

	private static String createAWSKey(int hours) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET"));
		cal.add(Calendar.HOUR_OF_DAY, hours);
		String key = dateFormat.format(cal.getTime());

		key += "Q" + (cal.get(Calendar.MINUTE) / 15 + 1);

		return key;
	}
}
