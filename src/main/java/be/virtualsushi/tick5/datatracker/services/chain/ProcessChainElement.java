package be.virtualsushi.tick5.datatracker.services.chain;

/**
 * Interface for Tweets and Tweet's urls processing in chain of responsibility
 * manner. Client should initialize chain of elements and call process() on root
 * element.
 * 
 * @author spv
 * 
 * @param <T>
 * @param <R>
 */
public interface ProcessChainElement<T, R> {

	public ProcessChainElement<T, R> setNext(ProcessChainElement<T, R> element);

	public R process(T object);

}
