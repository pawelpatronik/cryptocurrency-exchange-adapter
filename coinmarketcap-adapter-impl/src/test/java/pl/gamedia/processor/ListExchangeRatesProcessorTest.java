package pl.gamedia.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListExchangeRatesProcessorTest extends BaseTest {

	private ListExchangeRatesProcessor processor;

	private Utilities utilities = new Utilities();

	@Mock
	private CryptocurrencyDataProvider cryptocurrencyDataProvider;

	@Before
	public void testSetup() {
		processor = new ListExchangeRatesProcessor(cryptocurrencyDataProvider, utilities);
	}

	@Test
	public void shouldRetrieveFilteredExchangeRatesFromCoinmarketcapWhenSourceIsCoinmarketcapAndFilterIsDefined() {
		// given
		String source = "BTC";
		List<String> filter = asList("ETH", "USDT");
		when(cryptocurrencyDataProvider.getExchangePairList(source, new HashSet<>(filter)))
				.thenReturn(Try.of(() -> load(FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME,
						new TypeReference<>() {})));

		// when
		ListExchangeRatesResponse response = processor.process(source, filter).get();

		// then
		assertEquals(load(LIST_EXCHANGE_RATES_SAMPLE_RESPONSE_JSON_PATHNAME, ListExchangeRatesResponse.class), response);
	}
}
