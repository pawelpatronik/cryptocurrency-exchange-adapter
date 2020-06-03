package pl.gamedia.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mortbay.util.SingletonList;
import pl.gamedia.BaseTest;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.utils.Utilities;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CryptocurrencyDataProviderTest extends BaseTest {

	public static final double DELTA = 0.0000000001;
	private CryptocurrencyDataProvider cryptocurrencyDataProvider;

	@Mock
	private CryptocurrencyMarketPairsLatestService cryptocurrencyMarketPairsLatestService;

	@Before
	public void testSetup() {
		cryptocurrencyDataProvider = new CryptocurrencyDataProvider(TEST_EXCHANGE_NAME, cryptocurrencyMarketPairsLatestService, new Utilities());
	}

	@Test
	public void shouldRetrieveDataFromCoinmarketcapAndConvertToExchangePairList() {
		// given
		String baseCurrency = "BTC";
		when(cryptocurrencyMarketPairsLatestService
				.cryptocurrencyMarketPairsLatest(eq(baseCurrency)))
				.thenReturn(Try.of(() -> load(
						CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME,
						CryptocurrencyMarketPairsLatestResponse.class)));

		// when
		Map<String, List<CurrencyExchangePairDto>> exchangePairList = cryptocurrencyDataProvider
				.getExchangePairList(baseCurrency, null).get()
				.stream()
				.collect(Collectors.groupingBy(CurrencyExchangePairDto::getQuote));

		// then
		Map<String, List<CurrencyExchangePairDto>> expected = load(
				TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME, new TypeReference<List<CurrencyExchangePairDto>>() {})
				.stream().collect(Collectors.groupingBy(CurrencyExchangePairDto::getQuote));

		expected.forEach((key, value) -> assertEquals(value.get(0).getRate(), exchangePairList.get(key).get(0).getRate(), DELTA));
	}

	@Test
	public void shouldRetrieveAndFilterDataFromCoinmarketcapAndConvertToExchangePairList() {
		// given
		String baseCurrency = "BTC";
		when(cryptocurrencyMarketPairsLatestService
				.cryptocurrencyMarketPairsLatest(eq(baseCurrency)))
				.thenReturn(Try.of(() -> load(
						CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME,
						CryptocurrencyMarketPairsLatestResponse.class)));

		// when
		Map<String, List<CurrencyExchangePairDto>> exchangePairList = cryptocurrencyDataProvider
				.getExchangePairList(baseCurrency, asList("ETH", "USDT")).get()
				.stream()
				.collect(Collectors.groupingBy(CurrencyExchangePairDto::getQuote));

		// then
		Map<String, List<CurrencyExchangePairDto>> expected = load(
				FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME, new TypeReference<List<CurrencyExchangePairDto>>() {})
				.stream().collect(Collectors.groupingBy(CurrencyExchangePairDto::getQuote));

		expected.forEach((key, value) -> assertEquals(value.get(0).getRate(), exchangePairList.get(key).get(0).getRate(), DELTA));
	}
}
