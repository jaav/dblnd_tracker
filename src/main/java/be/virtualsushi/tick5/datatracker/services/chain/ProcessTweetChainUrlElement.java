package be.virtualsushi.tick5.datatracker.services.chain;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import be.virtualsushi.tick5.datatracker.components.ImageDownloader;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTweetChainUrlElement extends AbstractProcessChainElement<Tweet> {

	private static final Logger log = LoggerFactory.getLogger(ProcessTweetChainUrlElement.class);

	private ProcessChainElement<Document, String> processUrlChainRoot;

	private Document document;

	public ProcessTweetChainUrlElement(ImageDownloader imageDownloader) {
		processUrlChainRoot = new ProcessUrlChainOpenTagElement();
		processUrlChainRoot.setNext(new ProcessUrlChainFacebookElement())
				.setNext(new ProcessUrlChainSchemaOrgElement())
				.setNext(new ProcessUrlChainCustomElement(imageDownloader));
	}

	@Override
	protected boolean canProcess(Tweet object) {
		boolean result = (object!=null && object.hasUrl());
		if (result) {
			try {
				document = Jsoup.connect(object.getFirstUrlObject().getValue()).get();
				log.warn("Fetching image from an attached URL object - "+object.getFirstUrlObject().getValue());
			} catch (IOException e) {
				log.warn("JSOUP did NOT find the remote document.");
				return false;
			}
		}
		log.warn("Returning "+result+" in the canProcess method.");
		return result;
	}

	@Override
	protected String doProcess(Tweet object) {
		return processUrlChainRoot.process(document);
	}

}
