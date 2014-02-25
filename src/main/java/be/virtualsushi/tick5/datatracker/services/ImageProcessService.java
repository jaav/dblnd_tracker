package be.virtualsushi.tick5.datatracker.services;

import java.io.File;
import java.util.concurrent.Future;

import be.virtualsushi.tick5.datatracker.model.Tweet;

public interface ImageProcessService {

	public Future<File> createTweetImage(Tweet tweet);
	public Future<File> createFilteredImages(String imgName, int filterset);

}
