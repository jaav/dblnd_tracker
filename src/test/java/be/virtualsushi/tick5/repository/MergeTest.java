package be.virtualsushi.tick5.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import be.virtualsushi.tick5.BaseDatatrackerTest;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import be.virtualsushi.tick5.datatracker.repositories.TwitterUserRepository;
import be.virtualsushi.tick5.datatracker.services.TweetProcessService;

public class MergeTest extends BaseDatatrackerTest {

	@Autowired
	private TweetProcessService tweetProcessService;

	@Autowired
	private Twitter twitter;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Test
	public void testTweetProcess() throws TwitterException {
		TwitterUser user = TwitterUser.fromUser(twitter.showUser("mrmrfrag"));
		if (twitterUserRepository.findOne(user.getId()) == null) {
			twitterUserRepository.save(user);
		}
		ResponseList<Status> statuses = twitter.getUserTimeline(user.getId());
		for (Status status : statuses) {
			if ((status.getURLEntities() != null && status.getURLEntities().length > 0) || (status.getHashtagEntities() != null && status.getHashtagEntities().length > 0)
					|| (status.getMediaEntities() != null && status.getMediaEntities().length > 0)) {
				tweetProcessService.processStatus(status, true);
				break;
			}
		}
	}
}
