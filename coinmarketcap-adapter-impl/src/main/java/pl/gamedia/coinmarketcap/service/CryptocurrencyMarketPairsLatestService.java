package pl.gamedia.coinmarketcap.service;


import io.vavr.control.Try;
import org.springframework.web.client.RestTemplate;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;

public interface CryptocurrencyMarketPairsLatestService {
	Try<CryptocurrencyMarketPairsLatestResponse> cryptocurrencyMarketPairsLatest(String symbol);
}
