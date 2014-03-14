package be.virtualsushi.tick5.datatracker.components.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.*;
import be.virtualsushi.tick5.datatracker.components.Tick5StatusListener;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import be.virtualsushi.tick5.datatracker.services.TweetProcessService;

@Component("twitterUserStatusListener")
public class Tick5StatusListenerImpl implements UserStreamListener, Tick5StatusListener {

	private static final Logger log = LoggerFactory.getLogger(Tick5StatusListenerImpl.class);

	@Autowired
	private TwitterStream twitterStream;

	@Autowired
	private TweetProcessService tweetProcessService;

	@PostConstruct
	public void init() {
		twitterStream.addListener(this);
	}

	@Override
	public void onException(Exception ex) {
		log.error("Error listening twitter statuses updates.", ex);
	}

	@Override
	public void onStatus(Status status) {
		//log.debug(status.toString());
		//log.debug("Normal processing of tweet {} from {}", status.getText(), status.getUser().getScreenName());
		tweetProcessService.processStatus(status, true);
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		tweetProcessService.deleteTweet(statusDeletionNotice.getStatusId());
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {

	}

	@Override
	public void onStallWarning(StallWarning warning) {
		log.warn(warning.getCode(), warning.getMessage());
	}

	@Override
	public void listen(List<TwitterUser> followings) {
		long[] follow = new long[followings.size()];
		for (int i = 0; i < follow.length; i++) {
			follow[i] = followings.get(i).getId();
		}
		twitterStream.filter(new FilterQuery(follow));
	}

	@Override
	public void halt() {
		twitterStream.shutdown();
	}

	/*@Override
	public void restart(List<TwitterUser> followings) {
		halt();
		listen(followings);
	}*/

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {

	}

	@Override
	public void onFriendList(long[] friendIds) {

	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		log.debug("Favouriting tweet {} from {}", favoritedStatus.getText(), favoritedStatus.getUser().getScreenName());
		tweetProcessService.processStatus(favoritedStatus, true);
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

	}

	@Override
	public void onFollow(User source, User followedUser) {

	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {

	}

	@Override
	public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {

	}

	@Override
	public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {

	}

	@Override
	public void onUserListSubscription(User subscriber, User listOwner, UserList list) {

	}

	@Override
	public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {

	}

	@Override
	public void onUserListCreation(User listOwner, UserList list) {

	}

	@Override
	public void onUserListUpdate(User listOwner, UserList list) {

	}

	@Override
	public void onUserListDeletion(User listOwner, UserList list) {

	}

	@Override
	public void onUserProfileUpdate(User updatedUser) {

	}

	@Override
	public void onBlock(User source, User blockedUser) {

	}

	@Override
	public void onUnblock(User source, User unblockedUser) {

	}

}
