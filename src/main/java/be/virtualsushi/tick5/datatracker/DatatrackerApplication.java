package be.virtualsushi.tick5.datatracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import be.virtualsushi.tick5.datatracker.components.ImageFilter;
import be.virtualsushi.tick5.datatracker.model.Tweet;
import be.virtualsushi.tick5.datatracker.repositories.TweetRepository;
import be.virtualsushi.tick5.datatracker.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;

public class DatatrackerApplication {

	private static final int FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;
	private static final int FIFTEEN_MINUTES_MILLISECONDS = 15 * 60 * 1000;

	private static final float BESTSELLER_LIMIT_FACTOR = 0.2f;
	private static final Logger log = LoggerFactory.getLogger(DatatrackerApplication.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DatatrackerApplicationFactory.class);
			if(args!=null && args.length==3 && "addlist".equals(args[0])){
				final DataCreationService dataCreationService = applicationContext.getBean(DataCreationService.class);
				dataCreationService.addListMembers(args[1], args[2]);
			}
			else{
				applicationContext.getBean(DataCreationService.class).startDatatracking();
				//Start timer for cleanup job
				//final CleanupService cleanupService = applicationContext.getBean(CleanupService.class);
				//cleanupService.clean(createAWSKey(-4));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Datatracker application error:", e);
		}

	}

	/*private static Date getNextQuarter(){
		Calendar hereAndNow = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET"));
		hereAndNow.add(Calendar.MINUTE, 15-hereAndNow.get(Calendar.MINUTE)%15);
		hereAndNow.set(Calendar.SECOND, 0);
		DateFormat df = new SimpleDateFormat("dd/MM hh:mm:ss");
		log.debug("First analysis planned at "+df.format(hereAndNow.getTime())+ " and subsequently every " + FIFTEEN_MINUTES_MILLISECONDS/60000 + " minutes");
		return hereAndNow.getTime();
	}

	private static Date getLater(Date analyseTime, int howmuch){
		Calendar hereAndNow = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET"));
		hereAndNow.setTime(analyseTime);
		hereAndNow.add(Calendar.MINUTE, howmuch);
		DateFormat df = new SimpleDateFormat("dd/MM hh:mm:ss");
		log.debug("First images creation or publish job planned at "+df.format(hereAndNow.getTime())+ " and subsequently every " + FIFTEEN_MINUTES_MILLISECONDS/60000 + " minutes");
		return hereAndNow.getTime();
	}

	private static Date getCleanTime(){
		Calendar hereAndNow = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET"));
		int i = 14-hereAndNow.get(Calendar.MINUTE)%15;
		if(i<=2) return null;
		hereAndNow.add(Calendar.MINUTE, i);
		hereAndNow.set(Calendar.SECOND, 0);
		return hereAndNow.getTime();
	}*/



	private static String createAWSKey(int hours){


		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_");

		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("CET"));
		cal.add(Calendar.HOUR_OF_DAY, hours);
		String key = dateFormat.format(cal.getTime());

		key += "Q" + (cal.get(Calendar.MINUTE) / 15 + 1);

		return key;
	}
}
