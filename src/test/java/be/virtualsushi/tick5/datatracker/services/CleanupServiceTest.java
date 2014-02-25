package be.virtualsushi.tick5.datatracker.services;

import be.virtualsushi.tick5.BaseDatatrackerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CleanupServiceTest extends BaseDatatrackerTest {

	@Autowired
	private CleanupService cleanupService;

	@Test
	public void testClean() {
		String old_aws = createAWSKey(-4);
		cleanupService.clean(old_aws);
		Assert.assertNotNull(1);
	}




	private String createAWSKey(int hours){


		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

		GregorianCalendar cal = new GregorianCalendar(new Locale("nl", "BE"));
		cal.add(Calendar.HOUR_OF_DAY, hours);
		String key = dateFormat.format(cal.getTime());

		key += "Q" + (cal.get(Calendar.MINUTE) / 15 + 1);

		return key;
	}

}
