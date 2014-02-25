package be.virtualsushi.tick5.datatracker.services;

import twitter4j.Status;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;

public interface TweetProcessService {

	Tweet processStatus(Status status, boolean saveAfterProcess);

	void deleteTweet(Long tweetId);

}
