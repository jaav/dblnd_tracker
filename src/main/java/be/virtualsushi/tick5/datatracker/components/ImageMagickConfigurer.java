package be.virtualsushi.tick5.datatracker.components;

import javax.annotation.PostConstruct;

import org.im4java.process.ProcessStarter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageMagickConfigurer {

	@Value("${imagemagick.path}")
	private String executablesGlobalSearchPath;

	@PostConstruct
	public void init() {
		ProcessStarter.setGlobalSearchPath(executablesGlobalSearchPath);
	}

}
