package be.virtualsushi.tick5.datatracker.services.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.virtualsushi.tick5.datatracker.services.PublishService;

@Service("publishService")
public class PublishServiceImpl implements PublishService {

	private static final Logger log = LoggerFactory.getLogger(PublishServiceImpl.class);



	@Value("${web.root.folder}")
	private String imagesDirectoryName;

	@Override
	public void publish(String aws_key) {
		DateFormat fmt = new SimpleDateFormat("hh:mm:ss.SSS");
		log.debug("Start publishing content now. "+fmt.format(new Date()));
		try {
			FileWriter js = new FileWriter(new File(imagesDirectoryName+"/js", "pub.js"), false);
			String content = "var pub_key = '"+aws_key+"';";
			IOUtils.write(content, js);
			js.flush();
			js.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

}
