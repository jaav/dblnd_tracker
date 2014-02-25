package be.virtualsushi.tick5.datatracker.components;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HttpClientIdleConnectionsMonitor {

	@Autowired
	private ClientConnectionManager connectionManager;

	@Scheduled(fixedRate = 5000)
	public void handleIdleConnections() {
		connectionManager.closeExpiredConnections();
		connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
	}

}
