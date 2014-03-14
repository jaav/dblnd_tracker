package be.virtualsushi.tick5.datatracker.services.chain;

import be.virtualsushi.tick5.datatracker.components.ImageDownloader;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcessTweetChainTweepElement extends AbstractProcessChainElement<Tweet> {

	private static final Logger log = LoggerFactory.getLogger(ProcessTweetChainTweepElement.class);

	private String imgUrl;

	@Override
	protected boolean canProcess(Tweet tweet) {
		log.debug("There was NO URL object attached to the tweet. Now checking if an image can be found in the author of this tweet.");
		if(tweet!=null && tweet.getUser()!=null){
			log.debug("There is a user in this tweet. His name is "+ tweet.getUser().getScreenName());
			log.debug("This user's backgroundimage is "+tweet.getUser().getBackgroundImage());
			log.debug(".. does it contain 'profile_background_images'?");
			if(StringUtils.isNotBlank(tweet.getUser().getBackgroundImage()) && tweet.getUser().getBackgroundImage().contains("profile_background_images")){
				imgUrl = tweet.getUser().getBackgroundImage();
				return true;
			}
			log.debug("There was no background image. Is there a profile image?");
			log.debug("Profile image = "+tweet.getUser().getProfileImage());
			if(StringUtils.isNotBlank(tweet.getUser().getProfileImage())){
				imgUrl = tweet.getUser().getProfileImage();
				return true;
			}
			log.error("No image at all was found. This is not very likely!!");
		}
		return false;
	}

	@Override
	protected String doProcess(Tweet object) {
		log.debug("Processing image "+imgUrl);
		return imgUrl;
	}

}
