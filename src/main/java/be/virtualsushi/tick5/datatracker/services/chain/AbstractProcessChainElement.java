package be.virtualsushi.tick5.datatracker.services.chain;

/**
 * Some common implementation of {@link ProcessChainElement}. Gets object to
 * process, checks if object can be processed by it. If object cannot be
 * processed by this instance and nextElement is not null, then it will pass
 * object to nextElement or return null otherwise.
 * 
 * @author spv
 * 
 * @param <T>
 */
public abstract class AbstractProcessChainElement<T> implements ProcessChainElement<T, String> {

	private ProcessChainElement<T, String> nextElement;

	@Override
	public ProcessChainElement<T, String> setNext(ProcessChainElement<T, String> element) {
		nextElement = element;
		return nextElement;
	}

	@Override
	public String process(T object) {
		if (canProcess(object)) {
			return doProcess(object);
		}
		if (nextElement != null) {
			return nextElement.process(object);
		}
		return null;
	}

	protected abstract boolean canProcess(T object);

	protected abstract String doProcess(T object);
}
