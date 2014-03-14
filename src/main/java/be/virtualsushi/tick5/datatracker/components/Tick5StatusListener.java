package be.virtualsushi.tick5.datatracker.components;

import java.util.List;

import be.virtualsushi.tick5.datatracker.model.TwitterUser;

public interface Tick5StatusListener {

	void listen(List<TwitterUser> followings);
	void halt();
	//void restart(List<TwitterUser> followings);

}
