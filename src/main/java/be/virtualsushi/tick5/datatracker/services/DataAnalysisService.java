package be.virtualsushi.tick5.datatracker.services;

import be.virtualsushi.tick5.datatracker.model.Tweet;

import java.util.List;

public interface DataAnalysisService {

	void analyseTweets(String aws_key);
	void retweet();

}
