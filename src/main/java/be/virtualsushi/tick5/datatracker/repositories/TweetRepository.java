package be.virtualsushi.tick5.datatracker.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import be.virtualsushi.tick5.datatracker.model.Tweet;

@Repository
public interface TweetRepository extends Tick5Repository<Tweet> {



	public static final int RETWEET_MINIMUM_RETWEETS = 5;

	public static final int RETWEET_MAXIMUM_RETWEETS = 75;

	public static final int RETWEET_MINIMUM_RATE = 500;

	@Query("from Tweet where state=be.virtualsushi.tick5.datatracker.model.TweetStates.NOT_RATED")
	List<Tweet> getNotRatedTweets();

	@Query(value = "from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.RATED", countQuery = "select count(t.id) from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.RATED")
	Page<Tweet> getTopRatedTweets(Pageable pageable);

	@Query(value = "from Tweet t where t.state!=be.virtualsushi.tick5.datatracker.model.TweetStates.TOP_RATED" +
			" and t.rate>= be.virtualsushi.tick5.datatracker.repositories.TweetRepository.RETWEET_MINIMUM_RATE" +
			" and t.retweets>= be.virtualsushi.tick5.datatracker.repositories.TweetRepository.RETWEET_MINIMUM_RETWEETS" +
			" and t.retweets<= be.virtualsushi.tick5.datatracker.repositories.TweetRepository.RETWEET_MAXIMUM_RETWEETS")
	List<Tweet> getTopRatedTweetsForRetweeting();

	/**
	 * Selects very popular tweets (rate bigger than some predefined limit).
	 * 
	 * @param pageable
	 * @param limit
	 * @return
	 */
	@Query(value = "from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.TOP_RATED and t.rate>:limit", countQuery = "select count(t.id) from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.TOP_RATED and t.rate>:limit")
	Page<Tweet> getBestsellerTweets(@Param("limit") int limit, Pageable pageable);

	@Query(value = "from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.TOP_RATED")
	List<Tweet> getToppers();

	@Query(value = "from Tweet t where t.state=be.virtualsushi.tick5.datatracker.model.TweetStates.RATED")
	List<Tweet> getRatedTweets();

	@Transactional
	@Modifying
	@Query(value = "update Tweet set quantity=quantity+1 where id=:id")
	void updateTweetQuantity(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query(value = "update Tweet set retweeted=true where id=:id")
	void setRetweeted(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query(value = "update Tweet set RATE=:rate, RECENCY_FACTOR=:recency, TWEET_STATE=:state where id=:id")
	void rateTweet(@Param("id") Long id, @Param("rate") Integer rate, @Param("state") String state, @Param("recency") Float recency);


	@Transactional
	@Modifying
	@Query(value = "update Tweet set published=true where id=:id")
	void setPublished(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query(value = "delete from Tweet where state=be.virtualsushi.tick5.datatracker.model.TweetStates.TOP_RATED")
	void deleteTopRatedTweets();

	@Transactional
	@Modifying
	@Query(value = "delete from Tweet where state=be.virtualsushi.tick5.datatracker.model.TweetStates.RATED")
	void deleteRatedTweets();

	@Transactional
	@Modifying
	@Override
	@Query(value = "delete from Tweet where id=:tweet.getId()")
	void delete(Tweet tweet);

}
