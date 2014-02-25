package be.virtualsushi.tick5.datatracker.components.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.virtualsushi.tick5.datatracker.components.ImageDownloader;

@Component("imageDownloader")
public class ImageDownloaderImpl implements ImageDownloader {

	private static final Logger log = LoggerFactory.getLogger(ImageDownloaderImpl.class);

	@Autowired
	private HttpClient httpClient;

	@Value("${storage.directory.name}")
	private String storageDirectoryName;

	@Value("${images.folder}")
	private String imagesDirectoryName;

	private File storageRoot;

	@PostConstruct
	public void initImageDirectory() {
		storageRoot = new File(imagesDirectoryName, storageDirectoryName);
		storageRoot.mkdirs();
	}

	@Override
	public File downloadImage(String url) {
		if(StringUtils.isBlank(url)) return null;
		File result = null;
		InputStream input = null;
		FileOutputStream out = null;
		try {
			HttpEntity responseEntity = httpClient.execute(new HttpGet(url)).getEntity();
			String fileName = UUID.randomUUID().toString().replaceAll("-", "");
			String fileExtension = ".jpg";
			if ("image/png".equals(responseEntity.getContentType())) {
				fileExtension = ".png";
			}
			result = new File(storageRoot, fileName + fileExtension);
			input = responseEntity.getContent();
			out = new FileOutputStream(result);
			IOUtils.copy(input, out);
			EntityUtils.consumeQuietly(responseEntity);
			log.debug("result.length() = " + result.length());
			log.debug("result.getName() = " + result.getName());
		} catch (Exception e) {
			log.error("Error downloading file. url - " + url, e);
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(out);
		}
		return result;
	}

	@Override
	public BufferedImage downloadImageTemporarily(String url) {
		BufferedImage result = null;
		if(StringUtils.isNotBlank(url)){
			try {
				HttpEntity responseEntity = httpClient.execute(new HttpGet(url)).getEntity();
				result = ImageIO.read(responseEntity.getContent());
				EntityUtils.consumeQuietly(responseEntity);
			} catch (Exception e) {
				log.error("Error downloading file. url - " + url, e);
			}
		}
		return result;
	}
}
