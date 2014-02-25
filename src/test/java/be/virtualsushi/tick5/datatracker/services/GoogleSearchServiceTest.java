package be.virtualsushi.tick5.datatracker.services;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import be.virtualsushi.tick5.BaseDatatrackerTest;

public class GoogleSearchServiceTest extends BaseDatatrackerTest {

	@Autowired
	private GoogleSearchService googleSearchService;

	@Test
	public void testSearch() {
//		String result = googleSearchService.searchForImage("flowers");
//		Assert.assertNotNull(result);
//		String result2 = googleSearchService.searchForImage("flowers", "house");
//		//Assert.assertEquals(result, result2);
//		String result3 = googleSearchService.searchForImage("@flowers", "tron");
//		Assert.assertNotNull(result3);
		String result4 = googleSearchService.searchForImage("@SamynWetstraat @lindewin was dat zonder sarcastische toon? #dtv #villapolitica".split(" "));
		Assert.assertNotNull(result4);
	}

}
