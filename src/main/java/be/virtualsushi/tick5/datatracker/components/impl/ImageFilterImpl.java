package be.virtualsushi.tick5.datatracker.components.impl;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import be.virtualsushi.tick5.datatracker.components.StreamGobbler;
import be.virtualsushi.tick5.datatracker.services.DataAnalysisService;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import be.virtualsushi.tick5.datatracker.components.ImageFilter;

/**
 * Creates instagram like photo: 612x612
 *
 * @author spv
 */
@Component("imageFilter")
public class ImageFilterImpl implements ImageFilter {

	private static final Logger log = LoggerFactory.getLogger(ImageFilterImpl.class);

	private static final int IMAGE_SIZE = 480;
	private static final int QUALITY = 50;
	private static final String SQUARED_IMAGE = "squared";
	private static final String FILTER_LAYER = "layer";

	@Autowired
	private Environment env;


	@Value("${storage.directory.name}")
	private String storageDirectoryName;

	@Value("${images.folder}")
	private String imagesDirectoryName;

	@Value("${imagemagick.path}")
	private String magickPath;

	@Value("${filters.path}")
	private String filtersPath;


	@Override
	public void applyFancyFilters(String imageName, int filterset) {
		DateFormat fmt = new SimpleDateFormat("hh:mm:ss.SSS");
		log.debug("Start creating fancy images now. "+fmt.format(new Date()));
		String filters_def = env.getProperty("filters."+filterset);
		String[] filters = filters_def.split(",");
		for (int i = 0; i < filters.length; i++) {
			String filter = filters[i];
			try {
				//applyFirstFilter(imageName, filter);
				applyFilter(imageName, filter);

			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/*@Override
	public void applySquareFilter(File imageFile) {
		String imageName = imageFile.getName().substring(0, imageFile.getName().lastIndexOf("."));
		applyCropResize(imageFile, imageName);
	}*/

	@Override
	public void applySquareFilter(String imageName) {
		DateFormat fmt = new SimpleDateFormat("hh:mm:ss.SSS");
		log.debug("Start creating squared images now. "+fmt.format(new Date()));
		File storageRoot = new File(imagesDirectoryName, storageDirectoryName);
		File imageFile = new File(storageRoot, imageName+".jpg");
		applyCropResize(imageFile, imageName);
	}

	public boolean applyFilter(String imageName, String filter){
		String filters_def;
		if("vintage2".equals(filter))
			filters_def = MessageFormat.format(env.getProperty("filter." + filter),
				imagesDirectoryName + "/" + imageName + "_" + SQUARED_IMAGE + ".jpg",
			  filtersPath+"canvas.jpg",
				imagesDirectoryName + "/" + imageName + "_" + filter + ".jpg");
		else if("vintage3".equals(filter))
			filters_def = MessageFormat.format(env.getProperty("filter." + filter),
				imagesDirectoryName + "/" + imageName + "_" + SQUARED_IMAGE + ".jpg",
				filtersPath+"grunge.jpg",
				imagesDirectoryName + "/" + imageName + "_" + filter + ".jpg");
		else
			filters_def = MessageFormat.format(env.getProperty("filter." + filter),
				imagesDirectoryName + "/" + imageName + "_" + SQUARED_IMAGE + ".jpg",
				imagesDirectoryName + "/" + imageName + "_" + filter + ".jpg");
		ArrayList command = new ArrayList();
		addFilterCommands(filtersPath + filters_def, command);
		return exec((String[]) command.toArray(new String[1]), false);
	}

	private void addFilterCommands(String commands, ArrayList command){
		String[] commandParts = commands.split(" ");
		for (int i = 0; i < commandParts.length; i++) {
			String commandPart = commandParts[i];
			command.add(commandPart);
		}
	}

	private void showCommand(String[] commands){
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < commands.length; i++) {
			String s = commands[i];
			sb.append(commands[i]).append(" ");
		}
		log.debug(sb.toString());
	}

	private boolean applyCropResize(File imageFile, String imageName) {
		ArrayList command = new ArrayList(13);


		command.add(magickPath+"/convert");
		command.add(imageFile.getAbsolutePath());
		command.add("-auto-level");
		command.add("-auto-orient");
		command.add("-thumbnail");
		command.add(IMAGE_SIZE + "x" + IMAGE_SIZE + "^");
		command.add("-gravity");
		command.add("center");
		command.add("-extent");
		command.add(IMAGE_SIZE + "x" + IMAGE_SIZE);
		command.add("-quality");
		command.add("" + QUALITY);
		command.add(imagesDirectoryName + "/" + imageName + "_" + SQUARED_IMAGE + ".jpg");

		return exec((String[]) command.toArray(new String[1]), true);
	}

	private boolean exec(String[] command, boolean wait) {
		Process proc;
		//String[] env = {"MAGICK_HOME=\"/usr/local/ImageMagick\""};

		try {
			proc = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			System.out.println("IOException while trying to execute " + Arrays.toString(command));
			return false;
		}

		//streamgobbler consumes any error or input streams to prevent the process from hanging.
		int exitStatus = -1;
		if(wait){
			new StreamGobbler(proc.getErrorStream());
			new StreamGobbler(proc.getInputStream());

			try {
				exitStatus = proc.waitFor();
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}

			if (exitStatus != 0) {
				log.error("Error executing command: " + exitStatus);
				log.error("Error processing "+Arrays.toString(command));
			}
		}
		showCommand(command);
		return exitStatus == 0;
	}
}
