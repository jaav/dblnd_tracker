package be.virtualsushi.tick5;

import be.virtualsushi.tick5.datatracker.DatatrackerApplicationFactory;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DatatrackerApplicationFactory.class })
public class BaseDatatrackerTest {

}
