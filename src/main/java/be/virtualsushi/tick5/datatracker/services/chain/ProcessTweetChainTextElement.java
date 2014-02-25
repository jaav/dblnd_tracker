package be.virtualsushi.tick5.datatracker.services.chain;

import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;

public class ProcessTweetChainTextElement extends AbstractProcessChainElement<Tweet> {

	private GoogleSearchService googleSearchService;

	public ProcessTweetChainTextElement(GoogleSearchService googleSearchService) {
		this.googleSearchService = googleSearchService;
	}

	@Override
	protected boolean canProcess(Tweet object) {
		return object!=null;
	}

	@Override
	protected String doProcess(Tweet object) {
		return googleSearchService.searchForImage(object.getText());
	}

}
