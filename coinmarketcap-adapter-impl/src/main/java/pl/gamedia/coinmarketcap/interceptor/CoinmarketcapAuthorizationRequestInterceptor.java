package pl.gamedia.coinmarketcap.interceptor;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import pl.gamedia.exception.DataRetrievalException;

import java.io.IOException;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@Slf4j
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
		return Try.of(() -> execution.execute(request, body))
				.onSuccess(response -> log.debug("Successful client call execution: " + response.toString()))
				.onFailure(response -> log.debug("Failure during client call execution: " + response.toString()))
				.mapFailure(Case($(instanceOf(InterruptedException.class)), ex -> new DataRetrievalException("Failure during client call execution", ex)))
				.get();
	}
}
