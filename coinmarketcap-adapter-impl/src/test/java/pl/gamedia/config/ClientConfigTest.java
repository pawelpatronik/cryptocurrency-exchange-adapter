package pl.gamedia.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import pl.gamedia.coinmarketcap.interceptor.CoinmarketcapAuthorizationRequestInterceptor;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {ClientConfig.class})
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"source.api.coinmarketcap.endpoint=test.coinmarketcap.com",
		"source.api.coinmarketcap.connection.timeout.millis=10000"
})
@Import(CoinmarketcapAuthorizationRequestInterceptor.class)
public class ClientConfigTest {

	@Autowired
	private RestTemplate restTemplate;

	@Test
	public void shouldCreateRestTemplateBean() {
		assertNotNull(restTemplate);
	}
}
