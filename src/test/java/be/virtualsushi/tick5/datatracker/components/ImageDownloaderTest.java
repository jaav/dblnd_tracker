package be.virtualsushi.tick5.datatracker.components;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import be.virtualsushi.tick5.BaseDatatrackerTest;

public class ImageDownloaderTest extends BaseDatatrackerTest {

	private static final String IMAGE_URL = "http://qph.cf.quoracdn.net/main-qimg-f78d9ff3b50fe2629336a6562bceffb5";

	@Autowired
	private ImageDownloader imageDownloader;

	@Test
	public void testDownloadImage() {
		File result = imageDownloader.downloadImage(IMAGE_URL);
		Assert.assertTrue(result.exists());
	}

	@Test
	public void testDownloadImageTemporarily() {
		BufferedImage image = imageDownloader.downloadImageTemporarily(IMAGE_URL);
		Assert.assertEquals(485, image.getHeight());
		Assert.assertEquals(485, image.getWidth());
	}

}
