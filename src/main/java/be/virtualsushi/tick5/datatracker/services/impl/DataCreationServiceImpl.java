package be.virtualsushi.tick5.datatracker.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import be.virtualsushi.tick5.datatracker.components.Tick5StatusListener;
import be.virtualsushi.tick5.datatracker.model.TweepTypes;
import be.virtualsushi.tick5.datatracker.model.TwitterUser;
import be.virtualsushi.tick5.datatracker.repositories.TwitterUserRepository;
import be.virtualsushi.tick5.datatracker.services.DataCreationService;

@Service("dateCreationService")
public class DataCreationServiceImpl implements DataCreationService {

	private static final Logger log = LoggerFactory.getLogger(DataCreationServiceImpl.class);

	@Autowired
	private TwitterUserRepository twitterUserRepository;

	@Autowired
	private Twitter twitter;

	@Autowired
	private Tick5StatusListener tick5StatusListener;

	@Override
	public void startDatatracking() throws TwitterException {
		List<TwitterUser> existingUsers = twitterUserRepository.findAllListMembers(new PageRequest(0, 4000)).getContent();
		if (existingUsers == null) {
			existingUsers = new ArrayList<TwitterUser>();
		}
		// Check if new users were added to the list and if so, store them in
		// the DB
		/*if (init)
			updateListMembers(existingUsers);*/
		// Now start listening for new tweets from all these users
		tick5StatusListener.listen(existingUsers);
	}

	@Override
	public void restartDataTracking() {
		Page<TwitterUser> existingUsers = twitterUserRepository.findAllListMembers(new PageRequest(0, 4000));
		tick5StatusListener.halt();
		tick5StatusListener.listen(existingUsers.getContent());
	}

	@Override
	public void addListMembers(String tweep, String listName) throws TwitterException {
		final List<TwitterUser> users = new ArrayList<TwitterUser>();
		long cursor = -1;
		while (cursor != 0) {
			PagableResponseList<User> members = twitter.getUserListMembers(tweep, listName, cursor);
			for (User member : members) {
				TwitterUser user = TwitterUser.fromUser(member);
				TwitterUser existingUser = twitterUserRepository.findOne(user.getId());
				if (existingUser == null) {
					user.setListMember(true);
					user.setType(TweepTypes.IMPORTED);
					users.add(user);
				}
			}
			cursor = members.getNextCursor();
		}
		twitterUserRepository.save(users).iterator();
	}

	/*private void updateListMembers(List<TwitterUser> existingUsers) throws TwitterException {
		long cursor = -1;
		while (cursor != 0) {
			PagableResponseList<User> members = twitter.getUserListMembers(listOwnerName, listSlug, cursor);
			readListMembers(members, existingUsers);
			cursor = members.getNextCursor();
		}
	}*/

	private void readListMembers(PagableResponseList<User> members, List<TwitterUser> existingUsers) {
		final List<TwitterUser> users = new ArrayList<TwitterUser>();
		for (User member : members) {
			TwitterUser user = TwitterUser.fromUser(member);
			if (!existingUsers.contains(user)) {
				TwitterUser existingUser = twitterUserRepository.findOne(user.getId());
				if (existingUser != null) {
					existingUser.setListMember(true);
					existingUser.setType(TweepTypes.MEMBER);
					users.add(existingUser);
				} else {
					user.setListMember(true);
					user.setType(TweepTypes.MEMBER);
					users.add(user);
				}
			}
		}
		CollectionUtils.addAll(existingUsers, twitterUserRepository.save(users).iterator());
		// log.info(users.size() + " new members added.");
	}

}
