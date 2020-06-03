package pl.gamedia.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GetExchangeForecastProcessorTest extends BaseTest {

	public static final double FEE_AMOUNT_PERCENTAGE = 1;
	public static final double DOUBLE_COMPARISON_DELTA = 0.000000001;

	private GetExchangeForecastProcessor processor;

	@Mock
	private CryptocurrencyDataProvider cryptocurrencyDataProvider;

	@Before
	public void testSetup() {
		processor = new GetExchangeForecastProcessor(cryptocurrencyDataProvider, new Utilities(), FEE_AMOUNT_PERCENTAGE);
	}

	@Test
	public void shouldGetExchangeForecastForTwoCurrencies() {
		// given
		CurrencyExchangeRequest request = load(SAMPLE_CURRENCY_EXCHANGE_REQUEST_JSON_PATHNAME, CurrencyExchangeRequest.class);

		when(cryptocurrencyDataProvider.getExchangePairList(
				request.getFrom(), asList(request.getTo().getCurrencyB(), request.getTo().getCurrencyC())))
				.thenReturn(Try.of(() -> load(FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME,
						new TypeReference<>() {})));

		// when
		CurrencyExchangeResponse response = processor.process(request).get();

		// then
		CurrencyExchangeResponse expected = load(SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME, CurrencyExchangeResponse.class);
		assertEquals(expected.getFrom(), response.getFrom());
		assertEquals(expected.getCurrencyB().getAmount(), response.getCurrencyB().getAmount(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyB().getFee(), response.getCurrencyB().getFee(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyB().getRate(), response.getCurrencyB().getRate(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyB().getResult(), response.getCurrencyB().getResult(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyC().getAmount(), response.getCurrencyC().getAmount(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyC().getFee(), response.getCurrencyC().getFee(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyC().getRate(), response.getCurrencyC().getRate(), DOUBLE_COMPARISON_DELTA);
		assertEquals(expected.getCurrencyC().getResult(), response.getCurrencyC().getResult(), DOUBLE_COMPARISON_DELTA);
	}
}
