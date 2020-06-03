package pl.gamedia.rest;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.boundary.api.CurrenciesApi;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.processor.GetExchangeForecastProcessor;
import pl.gamedia.processor.ListExchangeRatesProcessor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CryptocurrencyApiAdapterControllerTest extends BaseTest {

	@Mock
	private ListExchangeRatesProcessor listExchangeRatesProcessor;

	@Mock
	private GetExchangeForecastProcessor getExchangeForecastProcessor;

	private CurrenciesApi api;

	@Before
	public void testSetup() {
		api = new CryptocurrencyApiAdapterController(
				Executors.newSingleThreadExecutor(),
				listExchangeRatesProcessor,
				getExchangeForecastProcessor);
	}

	@Test
	public void shouldReturnValidSampleResponseForBTCSourceAndFilterCodes() throws ExecutionException, InterruptedException {
		// given
		List<String> filter = asList("USDT", "ETH");
		String source = "BTC";

		when(listExchangeRatesProcessor.process(eq(source), eq(filter)))
				.thenReturn(Try.of(() -> load(LIST_EXCHANGE_RATES_SAMPLE_RESPONSE_JSON_PATHNAME, ListExchangeRatesResponse.class)));

		// when
		ListExchangeRatesResponse asyncResponseEntity = api.currenciesCurrencyGet(source, filter).get().getBody();

		// then
		assertEquals(load(LIST_EXCHANGE_RATES_SAMPLE_RESPONSE_JSON_PATHNAME, ListExchangeRatesResponse.class), asyncResponseEntity);
	}

	@Test
	public void shouldReturnExchangeForecastForTwoCurrencies() throws ExecutionException, InterruptedException {
		// given
		CurrencyExchangeRequest currencyExchangeRequest =
				load(SAMPLE_CURRENCY_EXCHANGE_REQUEST_JSON_PATHNAME, CurrencyExchangeRequest.class);

		when(getExchangeForecastProcessor.process(eq(currencyExchangeRequest)))
				.thenReturn(Try.of(() -> load(SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME, CurrencyExchangeResponse.class)));

		// when
		CurrencyExchangeResponse asyncResponseEntity =
				api.currenciesExchangePost(currencyExchangeRequest).get().getBody();

		// then
		assertEquals(load(SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME, CurrencyExchangeResponse.class), asyncResponseEntity);
	}
}
