package pl.gamedia.rest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.gamedia.BaseTest;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;
import pl.gamedia.exception.ListExchangeRatesFailedException;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.file.Files.readString;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static pl.gamedia.coinmarketcap.service.impl.CryptocurrencyMarketPairsLatestServiceImpl.ENDPOINT_PATH;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"source.api.coinmarketcap.endpoint=localhost:8089"
		})
@TestPropertySource(locations = "classpath:test_application.properties")
public class CryptocurrencyApiAdapterControllerIntegrationTest extends BaseTest {

	private static final String HOST = "localhost";
	private static final int PORT = 8089;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);

	@Autowired
	private CryptocurrencyApiAdapterController cryptocurrencyApiAdapterController;

	@Autowired
	private CryptocurrencyMarketPairsLatestService service;

	@Before
	public void testSetup() {
		ReflectionTestUtils.setField(service, "endpointRoot", HOST + ":" + PORT);
	}

	@Test
	public void shouldListOfTwoExchangeRatesForBTCCurrency() throws IOException, ExecutionException, InterruptedException {
		// given
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withBody(readString(new File(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_ALL_JSON_PATHNAME).toPath(),
								StandardCharsets.US_ASCII))
						.withHeader("Content-Type", "application/json")
						.withStatus(200)));

		// when
		ListExchangeRatesResponse body = cryptocurrencyApiAdapterController.currenciesCurrencyGet("BTC", asList("ETH", "XLM")).get().getBody();

		// then
		assertNotNull(body);
		assertEquals(2, body.getRates().size());
	}

	@Test
	public void shouldNotFailWhenTwoSameCurrenciesArePassedToFilter() throws IOException, ExecutionException, InterruptedException {
		// given
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withBody(readString(new File(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_ALL_JSON_PATHNAME).toPath(),
								StandardCharsets.US_ASCII))
						.withHeader("Content-Type", "application/json")
						.withStatus(200)));

		// when
		ListExchangeRatesResponse body = cryptocurrencyApiAdapterController.currenciesCurrencyGet("BTC", asList("ETH", "XLM", "XLM")).get().getBody();

		// then
		assertNotNull(body);
	}

	@Test(expected = ConstraintViolationException.class)
	public void shouldFailWhenSingleLetterCurrencyCodeIsProvided() throws IOException {
		// given
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withBody(readString(new File(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_ALL_JSON_PATHNAME).toPath(),
								StandardCharsets.US_ASCII))
						.withHeader("Content-Type", "application/json")
						.withStatus(200)));

		// when
		cryptocurrencyApiAdapterController.currenciesCurrencyGet("B", asList("ETH", "XLM"));

		// then exception thrown
	}

	@Test(expected = ExecutionException.class)
	public void shouldFailWhenCoinmarketcapApiIsNonResponsive() throws ExecutionException, InterruptedException {
		// given
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withStatus(400)));

		// when

		CompletableFuture<ResponseEntity<ListExchangeRatesResponse>> responseEntityCompletableFuture =
				Try.of(() -> cryptocurrencyApiAdapterController.currenciesCurrencyGet("BTC", asList("ETH", "XLM")))
						.onFailure(throwable -> assertEquals(ListExchangeRatesFailedException.class, throwable.getCause().getClass()))
						.get();
		responseEntityCompletableFuture.get();

		// then exception thrown
	}

	@Test
	public void shouldSuccessfullyProvideExchangeForecast() throws ExecutionException, InterruptedException, IOException {
		// given
		stubFor(get(urlPathMatching(ENDPOINT_PATH + "*"))
				.willReturn(aResponse()
						.withBody(readString(new File(CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_ALL_JSON_PATHNAME).toPath(),
								StandardCharsets.US_ASCII))
						.withHeader("Content-Type", "application/json")
						.withStatus(200)));

		// when
		CurrencyExchangeRequest request = new CurrencyExchangeRequest()
				.amount("12938.2938")
				.from("BTC")
				.to(asList("XLM", "ETH", "USDT"));
		CurrencyExchangeResponse body = cryptocurrencyApiAdapterController.currenciesExchangePost(request).get().getBody();

		// then
		assertNotNull(body);
		assertEquals(3, body.getTo().size());
	}
}
