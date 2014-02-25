package be.virtualsushi.tick5.datatracker.components.impl;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import be.virtualsushi.tick5.datatracker.components.ShortUrlsProcessor;

@Component("shortUrlsProcessor")
public class ShortUrlsProcessorImpl implements ShortUrlsProcessor {

	private static final Logger log = LoggerFactory.getLogger(ShortUrlsProcessorImpl.class);

	@Autowired
	private HttpClient httpClient;

	@Override
	public String getRealUrl(String shortUrl) {
		try {
			HttpResponse response = httpClient.execute(new HttpGet(shortUrl), new BasicHttpContext());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
				shortUrl = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
			}
			EntityUtils.consume(response.getEntity());
		} catch (Exception e) {
			log.error("Error expanding url: " + shortUrl + " Reason: " + e.getMessage());
		}
		return shortUrl;
	}
}
