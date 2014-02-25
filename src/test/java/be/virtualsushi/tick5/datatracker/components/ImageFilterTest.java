package be.virtualsushi.tick5.datatracker.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import be.virtualsushi.tick5.BaseDatatrackerTest;
import be.virtualsushi.tick5.datatracker.services.DataAnalysisService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageFilterTest extends BaseDatatrackerTest {

	private static final String[] TEST_IMAGES = { "no_image.png"};//, "img1.jpg", "img2.png", "img3.jpg" };

	@Autowired
	private ImageFilter imageFilter;

	@Test
	public void testImageProcessing() throws IOException {

    //ProcessStarter.setGlobalSearchPath("/opt/ImageMagick");
		for (String imageName : TEST_IMAGES) {
			File imageFile = File.createTempFile("tmp", imageName);
			File thisTestImage = new File("/Users/jefw/Sites/tick5/repo/datatracker_images/"+imageName);
			FileOutputStream outFile = new FileOutputStream(thisTestImage);
			IOUtils.copyLarge(getClass().getClassLoader().getResourceAsStream(imageName), outFile);
			IOUtils.closeQuietly(outFile);

			double checker = Math.random();
			/*int filter;
			if(checker>=0 && checker<0.25) filter = DataAnalysisService.PINK_FILTER;
			else if(checker>=0.25 && checker<0.5) filter = DataAnalysisService.GREEN_FILTER;
			else if(checker>=0.5 && checker<0.75) filter = DataAnalysisService.BLUE_FILTER;
			else filter = DataAnalysisService.DARK_FILTER;*/
			imageFilter.applySquareFilter(imageName);
			imageFilter.applyFancyFilters(imageName, 1);

			//DARK == OK
			//GREEN = too rough
			//BLUE doesn't exist
			//PINK == OK

		}
	}

}
