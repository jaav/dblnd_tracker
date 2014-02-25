package be.virtualsushi.tick5.datatracker.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import be.virtualsushi.tick5.datatracker.repositories.TweetObjectRepository;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;

@Component("dataCleaner")
public class DataCleaner {

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private TweetObjectRepository tweetObjectRepository;

	/**
	 * Cleans database once per day at 4:30
	 * 
	 */
	@Scheduled(cron = "0 30 4 * * *")
	public void cleanDatabase() {
		tweetRepository.deleteAll();
		tweetObjectRepository.deleteAll();
	}

}
