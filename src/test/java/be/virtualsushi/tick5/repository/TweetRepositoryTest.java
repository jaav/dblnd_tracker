package be.virtualsushi.tick5.repository;

import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.model.TweetObject;
import be.virtualsushi.tick5.datatracker.repositories.TweetObjectRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import be.virtualsushi.tick5.BaseDatatrackerTest;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;

import java.util.ArrayList;
import java.util.List;

public class TweetRepositoryTest extends BaseDatatrackerTest {

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private TweetObjectRepository tweetObjectRepository;

	@Test
	public void testDelete() {
		List<Tweet> tweets = tweetRepository.getRatedTweets();
		List<Long> object_ids = new ArrayList<Long>();
		for (Tweet tweet : tweets) {
			for (TweetObject tweetObject : tweet.getObjects()) {
				object_ids.add(tweetObject.getId());
			}
		}
		tweetObjectRepository.deleteTweetObjects(object_ids);
		tweetRepository.deleteRatedTweets();
	}

	@Test
	public void testGetNotRated() {
		tweetRepository.getNotRatedTweets();
	}


}
