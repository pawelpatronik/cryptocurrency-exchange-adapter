package pl.gamedia.coinmarketcap.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CoinmarketcapAuthorizationRequestInterceptor implements ClientHttpRequestInterceptor {

	private final String apiKeyHeader;
	private final String apiKeyValue;

	public CoinmarketcapAuthorizationRequestInterceptor(
			@Value("${source.api.coinmarketcap.key.header}") String apiKeyHeader,
			@Value("${source.api.coinmarketcap.key.value}") String apiKeyValue) {
		this.apiKeyHeader = apiKeyHeader;
		this.apiKeyValue = apiKeyValue;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().set(apiKeyHeader, apiKeyValue);
		return execution.execute(request, body);
	}
}
