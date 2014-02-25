package be.virtualsushi.tick5.datatracker.services.chain;

import org.jsoup.nodes.Document;

public class ProcessUrlChainSchemaOrgElement extends AbstractUrlProcessChainElement {

	@Override
	protected String doProcess(Document object) {
		return getFoundElements().get(0).attr("src");
	}

	@Override
	protected String getSelector() {
		return "[itemprop=image]";
	}

}
