package pl.gamedia.coinmarketcap.service.impl;

import io.vavr.control.Try;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;

@Service
public class CryptocurrencyMarketPairsLatestServiceImpl implements CryptocurrencyMarketPairsLatestService {

	public final static String ENDPOINT_PATH = "/v1/cryptocurrency/market-pairs/latest";

	private final String endpointRoot;
	private final Integer entriesLimit;
	private final RestTemplate restTemplate;

	public CryptocurrencyMarketPairsLatestServiceImpl(
			@Value("${source.api.coinmarketcap.endpoint}") String endpointRoot,
			@Value("${source.api.coinmarketcap.data.limit}") Integer entriesLimit,
			RestTemplate restTemplate) {
		this.endpointRoot = endpointRoot;
		this.entriesLimit = entriesLimit;
		this.restTemplate = restTemplate;
	}

	@Override
	public Try<CryptocurrencyMarketPairsLatestResponse> cryptocurrencyMarketPairsLatest(String symbol) {
		UriComponents builder = UriComponentsBuilder
				.fromPath(ENDPOINT_PATH)
				.host(endpointRoot)
				.scheme(HttpHost.DEFAULT_SCHEME_NAME)
				.queryParam("symbol", symbol)
				.queryParam("limit", entriesLimit)
				.build();

		return Try.of(() -> restTemplate.getForObject(
				builder.toUriString(), CryptocurrencyMarketPairsLatestResponse.class));
	}
}
