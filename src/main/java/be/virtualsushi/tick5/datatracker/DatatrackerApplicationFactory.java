package be.virtualsushi.tick5.datatracker;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;

@Configuration
@ComponentScan(basePackages = { "be.virtualsushi.tick5.datatracker" })
@PropertySource("classpath:application.properties")
@ImportResource("META-INF/infrastructure.xml")
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class DatatrackerApplicationFactory {

	@Bean(name = "twitter")
	public Twitter getTwitter() {
		return TwitterFactory.getSingleton();
	}

	@Bean(name = "propertyConfigurer")
	public static PropertySourcesPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean(name = "twitterStream")
	public TwitterStream getTwitterStream() {
		return TwitterStreamFactory.getSingleton();
	}

	@Bean(name = "httpClient")
	public HttpClient getHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", false);
		DefaultHttpClient.setDefaultHttpParams(params);
		return new DecompressingHttpClient(new DefaultHttpClient(getClientConnectionManager(), params));
	}

	@Bean(name = "clientConnectionManager")
	public ClientConnectionManager getClientConnectionManager() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry, 30, TimeUnit.SECONDS);
		connectionManager.setMaxTotal(1000);
		connectionManager.setDefaultMaxPerRoute(250);
		return connectionManager;
	}

	@Bean(name = "restTemplate")
	public RestTemplate getRestTemplate() {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(getHttpClient()));
	}

	@Bean(name = "taskExecutor")
	public TaskExecutor getTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		return taskExecutor;
	}

	@Bean(name = "taskScheduler")
	public TaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(10);
		return taskScheduler;
	}

	@Bean(name = "amazonDynamoDbClient")
	public AmazonDynamoDBClient getAmazonDynamoDBClient(@Value("${aws.endpoint}") String endpoint, @Value("${aws.accessKey}") String accessKey, @Value("${aws.secretKey}") String secretKey) {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		client.setEndpoint(endpoint);
		return client;
	}

}
