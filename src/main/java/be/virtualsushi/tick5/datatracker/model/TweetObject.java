package be.virtualsushi.tick5.datatracker.model;

import org.codehaus.jackson.annotate.JsonUnwrapped;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class TweetObject extends BaseEntity implements HasQuantity {

	private static final long serialVersionUID = 2576971152325807097L;

	private static final int IMAGE_QUANTITY_FACTOR = 2;
	private static final int URL_QUANTITY_FACTOR = 1;
	private static final int HASHTAG_QUANTITY_FACTOR = 1;

	@JsonUnwrapped
	@ManyToOne
	@JoinColumn(name = "TWEET_ID")
	private Tweet tweet;

	@Column(name = "VALUE", length = 2048)
	private String value;

	@Column(name = "QUANTITY")
	private int quantity;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private TweetObjectTypes type;



	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TweetObjectTypes getType() {
		return type;
	}

	public void setType(TweetObjectTypes type) {
		this.type = type;
	}

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}

	@Override
	public void increaseQuantity(int amount) {
		quantity += amount;
	}

	public int getQuantityFactor() {
		switch (type) {
		case IMAGE:
			return IMAGE_QUANTITY_FACTOR;
		case URL:
			return URL_QUANTITY_FACTOR;
		default:
			return HASHTAG_QUANTITY_FACTOR;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TweetObject other = (TweetObject) obj;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
