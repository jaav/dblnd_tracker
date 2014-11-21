package be.virtualsushi.tick5.datatracker.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SelfTweet extends CustomIdBaseEntity {

	private static final Logger log = LoggerFactory.getLogger(SelfTweet.class);

	private static final long serialVersionUID = -5452953243879914217L;

	@Column(name = "TWEET_TEXT")
	private String text;

	@Column(name = "TWEEP")
	private String tweep;


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public static SelfTweet fromTweet(Tweet tweet) {
		SelfTweet selfTweet = new SelfTweet();
		selfTweet.setId(tweet.getId());
		selfTweet.setText(tweet.getText());
		selfTweet.setTweep(tweet.getTweep());
		return selfTweet;
	}

	public void setTweep(String tweep) {
		this.tweep = tweep;
	}
}
