package be.virtualsushi.tick5.datatracker.repositories.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import be.virtualsushi.tick5.datatracker.components.ImageExternalLinkGenerator;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.repositories.AwsRepository;
import be.virtualsushi.tick5.datatracker.repositories.TwitterUserRepository;

import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;

@Component
public class AwsRepositoryImpl implements AwsRepository {
	private static final Logger log = LoggerFactory.getLogger(AwsRepositoryImpl.class);

	@Autowired
	private AmazonDynamoDBClient dynamoDBClient;

	@Autowired
	private ImageExternalLinkGenerator imageExternalLinkService;

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Value("${dynamo.tableName}")
	private String tweetsTableName;

	@Autowired
	private Twitter twitter;

	/*
	 * @Override public void cleanTweets(String aws_key) { QueryRequest query =
	 * new QueryRequest(tweetsTableName, new AttributeValue().withS(aws_key));
	 * QueryResult result = dynamoDBClient.query(query);
	 *//*
		 * for (Map<String, AttributeValue> stringAttributeValueMap :
		 * result.getItems()) { Set<String> set =
		 * stringAttributeValueMap.keySet(); }
		 *//*
			 * for(int i = 1; i<6; i++){ DeleteItemRequest delete = new
			 * DeleteItemRequest().withTableName(tweetsTableName).withKey(new
			 * Key(new AttributeValue().withS(aws_key), new
			 * AttributeValue().withN(""+i)));
			 * dynamoDBClient.deleteItem(delete); } }
			 */

	@Override
	public void cleanTweets(String aws_key) {

		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
		/*
		 * DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		 * scanExpression.addFilterCondition("id", new Condition()
		 * .withComparisonOperator(ComparisonOperator.LT)
		 * .withAttributeValueList(new AttributeValue().withN(aws_key)));
		 */

		Condition scanFilterCondition = new Condition().withComparisonOperator(ComparisonOperator.LT.toString()).withAttributeValueList(new AttributeValue().withS(aws_key));
		Map<String, Condition> conditions = new HashMap<String, Condition>();
		conditions.put("id", scanFilterCondition);
		ScanRequest scanRequest = new ScanRequest().withTableName(tweetsTableName).withScanFilter(conditions).withAttributesToGet(Arrays.asList("id"));
		;

		ScanResult scanResult = dynamoDBClient.scan(scanRequest);

		for (Map<String, AttributeValue> item : scanResult.getItems()) {
			String keyToDelete = item.get("id").getS();
			log.debug("Deleting ... " + keyToDelete);
			for (int i = 1; i < 6; i++) {
				DeleteItemRequest delete = new DeleteItemRequest().withTableName(tweetsTableName).withKey(new Key(new AttributeValue().withS(keyToDelete), new AttributeValue().withN("" + i)));
				dynamoDBClient.deleteItem(delete);
			}
		}

	}

	@Override
	public void insertTweets(Map<Long, Tweet> tweets, String key) {

		int i = 1;
		for (Long aLong : tweets.keySet()) {
			Tweet tweet = tweets.get(aLong);
			try {
				Map<String, AttributeValue> tweetItem = new HashMap<String, AttributeValue>();
				tweetItem.put("id", new AttributeValue().withS(key));
				tweetItem.put("sequence", new AttributeValue().withN(String.valueOf(i)));
				tweetItem.put("author", new AttributeValue().withS(tweet.getUser().getScreenName()));
				tweetItem.put("tweet", new AttributeValue().withS(tweet.getText()));
				tweetItem.put("score", new AttributeValue().withN(String.valueOf(tweet.getRate())));
				// tweetItem.put("style", new
				// AttributeValue().withS(tweet.getStyle()));

				if (CollectionUtils.isNotEmpty(tweet.getHashtags())) {
					tweetItem.put("hashtags", new AttributeValue().withSS(new LinkedHashSet<String>(tweet.getHashtags())));
				}
				if (StringUtils.isNotBlank(tweet.getImage())) {
					tweetItem.put("image", new AttributeValue().withS(imageExternalLinkService.createLink(tweet.getImage())));
				}
				if (CollectionUtils.isNotEmpty(tweet.getUrls())) {
					tweetItem.put("urls", new AttributeValue().withSS(new LinkedHashSet<String>(tweet.getUrls())));
				}

				i++;

				PutItemRequest request = new PutItemRequest(tweetsTableName, tweetItem);
				dynamoDBClient.putItem(request);
			} catch (Exception e) {
				log.debug(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/*
	 * private String hashRemove(String tweet, List<String> hashtags,
	 * List<String> urls){ String cleaned = null;
	 * 
	 * 
	 * 
	 * return cleaned; }
	 */
}
