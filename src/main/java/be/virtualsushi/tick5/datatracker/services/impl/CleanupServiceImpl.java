package be.virtualsushi.tick5.datatracker.services.impl;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.model.TweetObject;
import be.virtualsushi.tick5.datatracker.repositories.AwsRepository;
import be.virtualsushi.tick5.datatracker.repositories.TweetObjectRepository;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;
import be.virtualsushi.tick5.datatracker.services.CleanupService;
import be.virtualsushi.tick5.datatracker.services.ImageProcessService;

@Service("cleanupService")
public class CleanupServiceImpl implements CleanupService {

	private static final Logger log = LoggerFactory.getLogger(CleanupServiceImpl.class);

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private TweetObjectRepository tweetObjectRepository;

	@Autowired
	private ImageProcessService imageProcessService;

	@Autowired
	private AwsRepository awsRepository;

	@Value("${images.folder}")
	private String imagesDirectoryName;

	FileFilter multiFilter = new FileFilter() {
		public boolean accept(File file) {
			// keep image files for 48 hours
			long before = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
			long after = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
			return file.lastModified() < before && file.lastModified() > after && !file.getName().startsWith("no_image") && !file.getName().startsWith("sponsor");
		}
	};

	@Override
	public void clean(String aws_key) {
		// Two minutes before making a new analysis, remove all rated tweets
		// All existing rated tweets that continue to be active have
		// automatically been set to not_rated
		DateFormat fmt = new SimpleDateFormat("hh:mm:ss.SSS");
		log.debug("Start cleaning now. " + fmt.format(new Date()));
		try {
			// tweetRepository.deleteTopRatedTweets();
			// tweetRepository.deleteRatedTweets();
			List<Tweet> tweets = tweetRepository.getRatedTweets();
			List<Long> object_ids = new ArrayList<Long>();
			for (Tweet tweet : tweets) {
				for (TweetObject tweetObject : tweet.getObjects()) {
					object_ids.add(tweetObject.getId());
				}
			}
			if (object_ids.size() > 0)
				tweetObjectRepository.deleteTweetObjects(object_ids);
			tweetRepository.deleteRatedTweets();
		} catch (Exception e) {
			log.error("Error deleting tweets.", e);
		}
		try {
			List<Tweet> toppers = tweetRepository.getToppers();
			List<Long> object_ids = new ArrayList<Long>();
			for (Tweet tweet : toppers) {
				for (TweetObject tweetObject : tweet.getObjects()) {
					object_ids.add(tweetObject.getId());
				}
			}
			if (object_ids.size() > 0)
				tweetObjectRepository.deleteTweetObjects(object_ids);
			tweetRepository.deleteTopRatedTweets();

			log.debug("Deleting all tweets below " + aws_key);
			awsRepository.cleanTweets(aws_key);
		} catch (Exception e) {
			log.error("Error deleting tweets.", e);
		}
		try {
			File imagesFolder = new File(imagesDirectoryName);
			File[] files = imagesFolder.listFiles(multiFilter);
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			File imagesFolder = new File(imagesDirectoryName+"/datatracker_files");
			File[] files = imagesFolder.listFiles(multiFilter);
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
