package be.virtualsushi.tick5.datatracker.services.chain;

import be.virtualsushi.tick5.datatracker.model.Tweet;

public class ProcessTweetChainImageElement extends AbstractProcessChainElement<Tweet> {

	@Override
	protected boolean canProcess(Tweet object) {
		return object!=null && object.hasImage();
	}

	@Override
	protected String doProcess(Tweet object) {
		return object.getFirstImageObject().getValue();
	}

}
