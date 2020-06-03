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
import pl.gamedia.calculator.TransactionCalculator;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GetExchangeForecastProcessorTest extends BaseTest {

	private GetExchangeForecastProcessor processor;

	@Mock
	private CryptocurrencyDataProvider cryptocurrencyDataProvider;

	@Mock
	private TransactionCalculator transactionCalculator;

	@Before
	public void testSetup() {
		processor = new GetExchangeForecastProcessor(cryptocurrencyDataProvider, transactionCalculator, new Utilities());
	}

	@Test
	public void shouldGetExchangeForecastForTwoCurrencies() {
		// given
		CurrencyExchangeRequest request = load(SAMPLE_CURRENCY_EXCHANGE_REQUEST_JSON_PATHNAME, CurrencyExchangeRequest.class);
		List<CurrencyExchangePairDto> pairDtos = load(
				FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME, new TypeReference<>() {});

		when(cryptocurrencyDataProvider.getExchangePairList(
				request.getFrom(), new HashSet<>(request.getTo())))
				.thenReturn(Try.of(() -> pairDtos));

		Map<String, CurrencyExchangeSummary> summaryMap = load(TEST_SUMMARY_MAP_PATHNAME, new TypeReference<>() {});
		when(transactionCalculator.toCurrencyExchangeSummaryMap(pairDtos, request.getAmount()))
				.thenReturn(summaryMap);

		// when
		CurrencyExchangeResponse response = processor.process(request).get();

		// then
		CurrencyExchangeResponse expected = load(SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME, CurrencyExchangeResponse.class);
		assertEquals(expected.getFrom(), response.getFrom());
		String firstCurrency = request.getTo().get(0);
		String secondCurrency = request.getTo().get(1);
		assertEquals(getExchangeSummary(expected, firstCurrency).getAmount(), getExchangeSummary(response, firstCurrency).getAmount());
		assertEquals(getExchangeSummary(expected, firstCurrency).getFee(), getExchangeSummary(response, firstCurrency).getFee());
		assertEquals(getExchangeSummary(expected, firstCurrency).getRate(), getExchangeSummary(response, firstCurrency).getRate());
		assertEquals(getExchangeSummary(expected, firstCurrency).getResult(), getExchangeSummary(response, firstCurrency).getResult());
		assertEquals(getExchangeSummary(expected, secondCurrency).getAmount(), getExchangeSummary(response, secondCurrency).getAmount());
		assertEquals(getExchangeSummary(expected, secondCurrency).getFee(), getExchangeSummary(response, secondCurrency).getFee());
		assertEquals(getExchangeSummary(expected, secondCurrency).getRate(), getExchangeSummary(response, secondCurrency).getRate());
		assertEquals(getExchangeSummary(expected, secondCurrency).getResult(), getExchangeSummary(response, secondCurrency).getResult());
	}
}
