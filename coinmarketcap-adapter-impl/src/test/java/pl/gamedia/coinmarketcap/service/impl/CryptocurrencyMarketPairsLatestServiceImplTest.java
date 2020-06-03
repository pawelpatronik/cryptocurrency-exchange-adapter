package pl.gamedia.coinmarketcap.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.gamedia.BaseTest;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static pl.gamedia.coinmarketcap.service.impl.CryptocurrencyMarketPairsLatestServiceImpl.ENDPOINT_PATH;

@RunWith(MockitoJUnitRunner.class)
public class CryptocurrencyMarketPairsLatestServiceImplTest extends BaseTest {

	private static final String TEST_ENDPOINT = "test.coinmarketcap.com";

	private CryptocurrencyMarketPairsLatestService service;

	@Mock
	private RestTemplate restTemplate;

	@Before
	public void testSetup() {
		service = new CryptocurrencyMarketPairsLatestServiceImpl(TEST_ENDPOINT, 5000, restTemplate);
	}

	@Test
	public void shouldReturnListOfAllMarketPairsForBTCSymbol() {
		// given
		String symbol = "BTC";
		String url = UriComponentsBuilder
				.fromUriString(TEST_ENDPOINT + ENDPOINT_PATH)
				.queryParam("symbol", symbol)
				.toUriString();
		CryptocurrencyMarketPairsLatestResponse mockedResponse =
				load(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME, CryptocurrencyMarketPairsLatestResponse.class);
		when(restTemplate.getForObject(anyString(), eq(CryptocurrencyMarketPairsLatestResponse.class)))
				.thenReturn(mockedResponse);

		// when
		CryptocurrencyMarketPairsLatestResponse response = service.cryptocurrencyMarketPairsLatest(symbol).get();

		// then
		assertEquals(load(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME, CryptocurrencyMarketPairsLatestResponse.class), response);
	}
}
