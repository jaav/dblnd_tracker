package be.virtualsushi.tick5.datatracker.components;

import be.virtualsushi.tick5.BaseDatatrackerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ShortUrlsProcessorTest extends BaseDatatrackerTest {

	@Autowired
	private ShortUrlsProcessor shortUrlsProcessor;

	@Test
	public void testShowUrlsProcessor() {
		Assert.assertEquals("http://www.meetup.com/Bay-Area-Scala-Enthusiasts/events/88585602/", shortUrlsProcessor.getRealUrl("http://buff.ly/Q0Iqdm"));
	}

}
