package be.virtualsushi.tick5.datatracker.components.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.virtualsushi.tick5.datatracker.components.ImageExternalLinkGenerator;

@Service("ImageExternalLinkService")
public class ImageExternalLinkGeneratorImpl implements ImageExternalLinkGenerator {

	@Value("${external.address}")
	private String externalAddress;

	@Override
	public String createLink(String localPath) {
		/*if (StringUtils.contains(localPath, ":")) {
			return externalAddress + StringUtils.substringAfter(localPath, ":");
		}
		return externalAddress + localPath;*/
		return localPath;
	}

}
