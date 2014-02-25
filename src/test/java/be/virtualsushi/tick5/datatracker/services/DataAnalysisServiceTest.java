package be.virtualsushi.tick5.datatracker.services;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import be.virtualsushi.tick5.BaseDatatrackerTest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataAnalysisServiceTest extends BaseDatatrackerTest {

	@Autowired
	private DataAnalysisService dataAnalysisService;

	@Test
	public void testDataAnalysis() {
		dataAnalysisService.analyseTweets(createAWSKey());
	}




	private String createAWSKey(){


		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

		Date sendDate = new Date();
		String key = dateFormat.format(sendDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sendDate);

		key += "Q" + (calendar.get(Calendar.MINUTE) / 15 + 1);

		return key;
	}

}
