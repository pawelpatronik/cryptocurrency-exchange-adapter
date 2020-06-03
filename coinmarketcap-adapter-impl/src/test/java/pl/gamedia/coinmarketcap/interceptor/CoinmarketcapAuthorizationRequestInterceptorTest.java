package pl.gamedia.coinmarketcap.interceptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.config.AsyncConfig;
import pl.gamedia.config.ClientConfig;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"source.api.coinmarketcap.key.header=TEST_KEY_HEADER",
		"source.api.coinmarketcap.key.value=random_string"
})
public class CoinmarketcapAuthorizationRequestInterceptorTest extends BaseTest {

	@Autowired
	private CoinmarketcapAuthorizationRequestInterceptor coinmarketcapAuthorizationRequestInterceptor;

	@Mock
	private ClientHttpRequestExecution execution;

	@Test
	public void shouldCreateCoinmarketcapAuthorizationRequestInterceptorBean() {
		assertNotNull(coinmarketcapAuthorizationRequestInterceptor);
	}
}
