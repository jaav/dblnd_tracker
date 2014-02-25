package be.virtualsushi.tick5.datatracker.services.chain;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUrlProcessChainElement extends AbstractProcessChainElement<Document> {

	private static final Logger log = LoggerFactory.getLogger(AbstractUrlProcessChainElement.class);

	private Elements foundElements;

	@Override
	protected boolean canProcess(Document object) {
		foundElements = object.select(getSelector());
		log.warn("Can I process for "+getSelector()+"? "+(!foundElements.isEmpty()));
		return !foundElements.isEmpty();
	}

	protected Elements getFoundElements() {
		return foundElements;
	}

	protected abstract String getSelector();

}
