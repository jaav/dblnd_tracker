package be.virtualsushi.tick5.datatracker.services.chain;

import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.services.GoogleSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessTweetChainHashtagElement extends AbstractProcessChainElement<Tweet> {

	private GoogleSearchService googleSearchService;

	public ProcessTweetChainHashtagElement(GoogleSearchService googleSearchService) {
		this.googleSearchService = googleSearchService;
	}

	@Override
	protected boolean canProcess(Tweet object) {
		return object!=null && object.hasHashtag();
	}

	@Override
	protected String doProcess(Tweet object) {
		List<String> tags = object.getHashtags();
		tags.addAll(getNames(object.getText()));
		return googleSearchService.searchForImage(tags);
	}

	private List<String> getNames(String tweetContent){
		List<String> names = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\w\\s+[A-Z]\\w*");
		Matcher matcher = pattern.matcher(tweetContent);
		while(matcher.find()){
        names.add(tweetContent.substring(matcher.start()+2, matcher.end()));
    }
		return names;
	}
}
