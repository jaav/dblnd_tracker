package be.virtualsushi.tick5.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import be.virtualsushi.tick5.BaseDatatrackerTest;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import be.virtualsushi.tick5.datatracker.repositories.AwsRepository;
import be.virtualsushi.tick5.datatracker.repositories.TwitterUserRepository;
import be.virtualsushi.tick5.datatracker.services.TweetProcessService;

public class AwsRepositoryTest extends BaseDatatrackerTest {

	@Autowired
	private AwsRepository awsRepository;

	@Autowired
	private Twitter twitter;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Autowired
	private TweetProcessService tweetProcessService;

	@Test
	public void testInsert() throws TwitterException, InterruptedException, ExecutionException {
		TwitterUser user = TwitterUser.fromUser(twitter.showUser("jaav"));
		if (twitterUserRepository.findOne(user.getId()) == null) {
			twitterUserRepository.save(user);
		}
		ResponseList<Status> statuses = twitter.getUserTimeline(user.getId(), new Paging(1, 5));
		List<Tweet> tweets = new ArrayList<Tweet>();
		for (Status status : statuses) {
			Tweet tweet = tweetProcessService.processStatus(status, false);
			tweet.setImage("numb");
			tweets.add(tweet);
		}
		//awsRepository.insertTweets(tweets, createAWSKey());
	}

	@Test
	public void testClean(){
		awsRepository.cleanTweets("2013_05_30_16_Q4");
	}



	private String createAWSKey(){


		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

		Date sendDate = new Date();
		String key = dateFormat.format(sendDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sendDate);

		key += "Q" + (calendar.get(Calendar.MINUTE) / 15 + 1);

		return key;
	}

}
