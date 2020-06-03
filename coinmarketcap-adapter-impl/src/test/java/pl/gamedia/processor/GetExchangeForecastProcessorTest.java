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
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.HashSet;

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
				request.getFrom(), new HashSet<>(request.getTo())))
				.thenReturn(Try.of(() -> load(FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME,
						new TypeReference<>() {})));

		// when
		CurrencyExchangeResponse response = processor.process(request).get();

		// then
		CurrencyExchangeResponse expected = load(SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME, CurrencyExchangeResponse.class);
		assertEquals(expected.getFrom(), response.getFrom());
		String firstCurrency = request.getTo().get(0);
		String secondCurrency = request.getTo().get(1);
		assertDoubleValue(getExchangeSummary(expected, firstCurrency).getAmount(), getExchangeSummary(response, firstCurrency).getAmount());
		assertDoubleValue(getExchangeSummary(expected, firstCurrency).getFee(), getExchangeSummary(response, firstCurrency).getFee());
		assertDoubleValue(getExchangeSummary(expected, firstCurrency).getRate(), getExchangeSummary(response, firstCurrency).getRate());
		assertDoubleValue(getExchangeSummary(expected, firstCurrency).getResult(), getExchangeSummary(response, firstCurrency).getResult());
		assertDoubleValue(getExchangeSummary(expected, secondCurrency).getAmount(), getExchangeSummary(response, secondCurrency).getAmount());
		assertDoubleValue(getExchangeSummary(expected, secondCurrency).getFee(), getExchangeSummary(response, secondCurrency).getFee());
		assertDoubleValue(getExchangeSummary(expected, secondCurrency).getRate(), getExchangeSummary(response, secondCurrency).getRate());
		assertDoubleValue(getExchangeSummary(expected, secondCurrency).getResult(), getExchangeSummary(response, secondCurrency).getResult());
	}

	private void assertDoubleValue(Double amount, Double amount2) {
		assertEquals(amount,
				amount2, DOUBLE_COMPARISON_DELTA);
	}

	private CurrencyExchangeSummary getExchangeSummary(CurrencyExchangeResponse response, String currency) {
		return response.getTo().get(currency);
	}
}
