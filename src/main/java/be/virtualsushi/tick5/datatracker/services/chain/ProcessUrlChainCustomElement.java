package be.virtualsushi.tick5.datatracker.services.chain;

import java.awt.image.BufferedImage;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import be.virtualsushi.tick5.datatracker.components.ImageDownloader;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUrlChainCustomElement extends AbstractProcessChainElement<Document> {

	private static final Logger log = LoggerFactory.getLogger(ProcessUrlChainCustomElement.class);

	private static final int DOCUMENT_IMGS_TO_CHECK_COUNT = 25;
	private static final int MIN_IMAGE_SIZE = 260;

	private ImageDownloader imageDownloader;

	private String imageUrl;

	public ProcessUrlChainCustomElement(ImageDownloader imageDownloader) {
		this.imageDownloader = imageDownloader;
	}

	@Override
	protected boolean canProcess(Document object) {
		log.warn("In 'canProcess' and object is not null? "+(object!=null));
		if(object==null) return false;

		Elements elements = object.select("img");
		int imgsCount = elements.size() < DOCUMENT_IMGS_TO_CHECK_COUNT ? elements.size() : DOCUMENT_IMGS_TO_CHECK_COUNT;
		for (int i = 0; i < imgsCount; i++) {
			if(elements.get(i).attr("src")!=null){
				String tmpImageUrl = object.absUrl(elements.get(i).attr("src"));
				BufferedImage image = imageDownloader.downloadImageTemporarily(tmpImageUrl);
				if (image != null && image.getHeight() > MIN_IMAGE_SIZE && image.getWidth() > MIN_IMAGE_SIZE) {
					imageUrl = tmpImageUrl;
					log.warn("We have an image!!! "+imageUrl);
					return true;
				}
			}
		}
		log.warn("We did NOT find an image in "+object.title()+". Now moving to processing tweet author ...");
		return false;
	}

	@Override
	protected String doProcess(Document object) {
		log.warn("Processing "+object.title());
		return imageUrl;
		// No luck with images let's ask google for help.
		/*elements = object.select("title");
		if(!elements.isEmpty())
			return googleSearchService.searchForImage(elements.get(0).ownText());
		else return "";*/
	}

}
