package be.virtualsushi.tick5.datatracker.repositories;

import java.util.List;
import java.util.Map;

import be.virtualsushi.tick5.datatracker.model.Tweet;

public interface AwsRepository {

	public void insertTweets(Map<Long, Tweet> tweets, String key);

	public void cleanTweets(String aws_key);

}
