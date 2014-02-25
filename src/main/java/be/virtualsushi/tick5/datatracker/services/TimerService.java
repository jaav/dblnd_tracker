package be.virtualsushi.tick5.datatracker.services;

public interface TimerService {

	void analyse();
	void createSquaredImages();
	void createFancyImages();
	void publish();
	void clean();
	void relaunch();

}
