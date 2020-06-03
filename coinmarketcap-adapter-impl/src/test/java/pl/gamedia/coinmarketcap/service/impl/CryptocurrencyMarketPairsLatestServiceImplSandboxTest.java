package pl.gamedia.coinmarketcap.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CryptocurrencyMarketPairsLatestServiceImplSandboxTest extends BaseTest {

	@Value("${source.api.coinmarketcap.data.limit}")
	private Integer entriesLimit;

	@Autowired
	private CryptocurrencyMarketPairsLatestService service;

	@Test
	public void shouldReturnListOfAllMarketPairsForBTCSymbolFromSandboxEnvironment() {
		// given
		String symbol = "BTC";

		// when
		CryptocurrencyMarketPairsLatestResponse response = service.cryptocurrencyMarketPairsLatest(symbol).get();

		// then
		Integer responseDataSize = response.getData().getMarketPairs().size();
		assertEquals(entriesLimit, responseDataSize);
	}
}
