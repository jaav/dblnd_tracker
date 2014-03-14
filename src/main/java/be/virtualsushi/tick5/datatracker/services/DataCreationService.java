package be.virtualsushi.tick5.datatracker.services;

import java.util.concurrent.ExecutionException;

import twitter4j.TwitterException;

public interface DataCreationService {

	void startDatatracking() throws TwitterException, InterruptedException, ExecutionException;

	void addListMembers(String tweep, String listName) throws TwitterException;

	void restartDataTracking();

}
