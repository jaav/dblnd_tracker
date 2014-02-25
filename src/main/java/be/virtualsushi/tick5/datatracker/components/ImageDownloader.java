package be.virtualsushi.tick5.datatracker.components;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageDownloader {

	public File downloadImage(String url);

	public BufferedImage downloadImageTemporarily(String url);

}
