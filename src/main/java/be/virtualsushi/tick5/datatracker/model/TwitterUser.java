package be.virtualsushi.tick5.datatracker.model;

import java.util.List;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import twitter4j.User;

@Entity
public class TwitterUser extends CustomIdBaseEntity {

	private static final long serialVersionUID = 3549168563451689799L;

	@JsonProperty("author")
	@Column(name = "NAME")
	private String name;

	@JsonIgnore
	@Column(name = "SCREEN_NAME")
	private String screenName;

	@JsonIgnore
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "user")
	private List<Tweet> tweets;

	@Column(name = "IS_LIST_MEMBER")
	private boolean listMember;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private TweepTypes type;

	@Column(name = "LANGUAGE")
	private String language;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "NUMBEROFTWEETS")
	private int numberoftweets;

	@Column(name = "RTS")
	private int rts;

	@Column(name = "MAX_FAVS")
	private int maxFavs;

	@Column(name = "MAX_RTS")
	private int maxRts;

	@Transient
	private String backgroundImage;

	@Transient
	private String profileImage;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public static TwitterUser fromUser(User user) {
		TwitterUser result = new TwitterUser();
		result.setId(user.getId());
		result.setScreenName(user.getScreenName());
		result.setName(user.getName());
		return result;
	}

	public List<Tweet> getTweets() {
		return tweets;
	}

	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}

	public int getRts() {
		return rts;
	}

	public void setRts(int rts) {
		this.rts = rts;
	}

	public boolean isListMember() {
		return listMember;
	}

	public void setListMember(boolean listMember) {
		this.listMember = listMember;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public TweepTypes getType() {
		return type;
	}

	public void setType(TweepTypes type) {
		this.type = type;
	}

	/*public int getNumberoftweets() {
		return numberoftweets;
	}

	public void setNumberoftweets(int numberoftweets) {
		this.numberoftweets = numberoftweets;
	}*/

	public int getMaxFavs() {
		return maxFavs;
	}

	public void setMaxFavs(int maxFavs) {
		this.maxFavs = maxFavs;
	}

	public int getMaxRts() {
		return maxRts;
	}

	public void setMaxRts(int maxRts) {
		this.maxRts = maxRts;
	}

	/*public String getLanguage() {
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
	}*/
}
