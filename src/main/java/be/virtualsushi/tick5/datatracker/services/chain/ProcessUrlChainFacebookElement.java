package be.virtualsushi.tick5.datatracker.services.chain;

import org.jsoup.nodes.Document;

public class ProcessUrlChainFacebookElement extends AbstractUrlProcessChainElement {

	@Override
	protected String doProcess(Document object) {
		return getFoundElements().get(0).attr("href");
	}

	@Override
	protected String getSelector() {
		return "link[rel=image_src]";
	}

}
