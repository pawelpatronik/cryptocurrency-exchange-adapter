package pl.gamedia.coinmarketcap.service.impl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.gamedia.BaseTest;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.file.Files.readString;
import static org.junit.Assert.assertEquals;
import static pl.gamedia.coinmarketcap.service.impl.CryptocurrencyMarketPairsLatestServiceImpl.ENDPOINT_PATH;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"source.api.coinmarketcap.endpoint=localhost:8080"
		})
public class CryptocurrencyMarketPairsLatestServiceImplIntegrationTest extends BaseTest {

	public static final int PORT = 8080;
	public static final String HOST = "localhost";

	@Autowired
	private CryptocurrencyMarketPairsLatestService service;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);

	@Test
	public void shouldReturnListOfAllMarketPairsForBTCSymbolFromMockedServer() throws IOException {
		// given
		ReflectionTestUtils.setField(service, "endpointRoot", HOST + ":" + PORT);
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withBody(readString(new File(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME).toPath(), StandardCharsets.US_ASCII))
						.withHeader("Content-Type", "application/json")
						.withStatus(200)));
		String symbol = "BTC";

		// when
		CryptocurrencyMarketPairsLatestResponse response = service.cryptocurrencyMarketPairsLatest(symbol).get();

		// then
		assertEquals(load(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME, CryptocurrencyMarketPairsLatestResponse.class), response);
	}
}
