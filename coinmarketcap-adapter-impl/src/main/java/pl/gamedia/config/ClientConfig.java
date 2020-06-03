package pl.gamedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import pl.gamedia.coinmarketcap.interceptor.CoinmarketcapAuthorizationRequestInterceptor;

import java.time.Duration;

@Configuration
public class ClientConfig {

	@Value("${source.api.coinmarketcap.endpoint}")
	private String coinmarketcapEndpoint;

	@Value("${source.api.coinmarketcap.connection.timeout.millis}")
	private Integer connectionTimeout;

	@Bean
	public RestTemplateBuilder restTemplateBuilder() {
		return new RestTemplateBuilder();
	}

	@Bean
	public RestTemplate coinmarketcapRestTemplate(
			RestTemplateBuilder restTemplateBuilder,
			CoinmarketcapAuthorizationRequestInterceptor coinmarketcapAuthorizationRequestInterceptor) {
		return restTemplateBuilder
				.rootUri(coinmarketcapEndpoint)
				.setConnectTimeout(Duration.ofMillis(connectionTimeout))
				.additionalInterceptors(coinmarketcapAuthorizationRequestInterceptor)
				.build();
	}
}
