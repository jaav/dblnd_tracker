package be.virtualsushi.tick5.datatracker.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

import twitter4j.Status;

/**
 * Tweet post json format:<br/>
 * [<br/>
 * {<br/>
 * "hashtags":["hash11","hash12","hash13"],<br/>
 * "urls":["http://a.realurl11.com","http://a.realurl12.com",
 * "http://a.realurl13.com"],<br/>
 * "tweet":
 * "This is the content of a tweet. Bla bla ... #hash11 #hash12 #hash13 http://t.co/123456 http://goo.gl/789456 http://bit.ly/3j4ir4"
 * ,<br/>
 * "author":"anAuthor1",<br/>
 * "image":"C:\\fakepath\\20121104_161954.jpg"<br/>
 * },<br/>
 * {<br/>
 * "hashtags":["hash21","hash22"],<br/>
 * "urls":["http://a.realurl21.com","http://a.realurl22.com"],<br/>
 * "tweet":
 * "This is the content of another tweet. Bla bla ... #hash21 #hash22 http://t.co/654321 http://goo.gl/ye87j"
 * ,<br/>
 * "author":"anAuthor2",<br/>
 * "image":"C:\\fakepath\\20121104_162553.jpg"<br/>
 * },<br/>
 * ]
 * 
 * @author spv
 * 
 */

@JsonIgnoreProperties(value = { "id", "objects", "quantity", "state", "lastModified" })
@Entity
public class Tweet extends CustomIdBaseEntity implements HasQuantity {

	private static final long serialVersionUID = -5452953243879914216L;

	private static final int TWEET_QUANTITY_FACTOR = 2;

	@JsonUnwrapped
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private TwitterUser user;

	@JsonProperty("tweet")
	@Column(name = "TWEET_TEXT")
	private String text;


	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "tweet", fetch = FetchType.EAGER)
	private List<TweetObject> objects;

	@Column(name = "QUANTITY")
	private int quantity;

	@Column(name = "TWEET_STATE")
	@Enumerated(EnumType.STRING)
	private TweetStates state;

	@Column(name = "RATE")
	private int rate;

	@Column(name = "IMAGE")
	private String image;

	@Column(name = "RECENCY_FACTOR")
	private float recencyFactor;

	@JsonProperty("style")
	@Column(name = "STYLE")
	private String style;

	@Column(name = "LANGUAGE")
	private String language;

	@Column(name = "LOCATION")
	private String location;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TwitterUser getUser() {
		return user;
	}

	public void setUser(TwitterUser user) {
		this.user = user;
	}

	@Override
	public void increaseQuantity(int amount) {
		quantity += amount;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void addObject(TweetObject object) {
		if (objects == null) {
			objects = new ArrayList<TweetObject>();
		}
		objects.add(object);
	}

	public void addObjects(List<TweetObject> objects) {
		if (this.objects == null) {
			this.objects = objects;
		} else {
			this.objects.addAll(objects);
		}
	}

	public List<TweetObject> getObjects() {
		return objects;
	}

	public void setObjects(List<TweetObject> objects) {
		this.objects = objects;
	}

	public static Tweet fromStatus(Status status, TwitterUser user) {
		Tweet tweet = new Tweet();
		tweet.setId(status.getId());
		StringBuilder tweetTextBuilder = new StringBuilder();
		/*String text = status.getText();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
				tweetTextBuilder.append(ch);
			}
		}
		tweet.setText(tweetTextBuilder.toString());*/
		tweet.setText(status.getText());
		tweet.setUser(user);
		//tweet.setLanguage(user.getLanguage());
		//tweet.setLocation(user.getLocation());
		return tweet;
	}

	public TweetStates getState() {
		return state;
	}

	public void setState(TweetStates state) {
		this.state = state;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public boolean hasImage() {
		return searchForType(TweetObjectTypes.IMAGE);
	}

	public boolean hasUrl() {
		return searchForType(TweetObjectTypes.URL);
	}

	public boolean hasHashtag() {
		return searchForType(TweetObjectTypes.HASHTAG);
	}

	private boolean searchForType(TweetObjectTypes type) {
		for (TweetObject object : objects) {
			if (type.equals(object.getType())) {
				return true;
			}
		}
		return false;
	}

	// @todo add this annotation to all props that need to be hidden
	@JsonIgnore
	public TweetObject getFirstImageObject() {
		return getFirstOfType(TweetObjectTypes.IMAGE);
	}

	public TweetObject getFirstUrlObject() {
		return getFirstOfType(TweetObjectTypes.URL);
	}

	private TweetObject getFirstOfType(TweetObjectTypes type) {
		for (TweetObject object : objects) {
			if (type.equals(object.getType())) {
				return object;
			}
		}
		return null;
	}

	@JsonProperty("hashtags")
	public List<String> getHashtags() {
		return getValuesByType(TweetObjectTypes.HASHTAG);
	}

	@JsonProperty("urls")
	public List<String> getUrls() {
		return getValuesByType(TweetObjectTypes.URL);
	}

	private List<String> getValuesByType(TweetObjectTypes type) {
		List<String> result = new ArrayList<String>();
		for (TweetObject object : objects) {
			if (type.equals(object.getType())) {
				result.add(object.getValue());
			}
		}
		return result;
	}

	public int getRawRate() {
		return getQuantity() * TWEET_QUANTITY_FACTOR;
	}

	@JsonProperty("image")
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public float getRecencyFactor() {
		return recencyFactor;
	}

	public void setRecencyFactor(float recencyFactor) {
		this.recencyFactor = recencyFactor;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
