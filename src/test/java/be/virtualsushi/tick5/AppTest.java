package be.virtualsushi.tick5;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

	@Test
	public void test() {
		String string = "blah+blah+blah+";
		System.out.println(string.substring(0, string.length() - 1));
		HashMap<Long, Integer> map = new HashMap<Long, Integer>();
		map.put(1l, 0);
		System.out.println(map.put(1l, map.get(1l) + 1));
		System.out.println(map.put(1l, map.get(1l) + 1));
		System.out.println(map.put(1l, map.get(1l) + 1));

		Date sendDate = new Date();
		String key = dateFormat.format(sendDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sendDate);

		key += "Q" + (calendar.get(Calendar.MINUTE) / 15 + 1);

		System.out.println(key);
	}
}
