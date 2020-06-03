package pl.gamedia.provider;

import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPair;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponseData;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.utils.Utilities;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CryptocurrencyDataProvider {

	private final String exchangeName;

	private final CryptocurrencyMarketPairsLatestService cryptocurrencyMarketPairsLatestService;
	private final Utilities utilities;

	public CryptocurrencyDataProvider(
			@Value("${source.api.coinmarketcap.source.exchange}") String exchangeName,
			CryptocurrencyMarketPairsLatestService cryptocurrencyMarketPairsLatestService,
			Utilities utilities) {
		this.exchangeName = exchangeName;
		this.cryptocurrencyMarketPairsLatestService = cryptocurrencyMarketPairsLatestService;
		this.utilities = utilities;
	}

	public Try<List<CurrencyExchangePairDto>> getExchangePairList(String baseCurrency, List<String> currencyFilter) {
		return cryptocurrencyMarketPairsLatestService
				.cryptocurrencyMarketPairsLatest(baseCurrency)
				.map(CryptocurrencyMarketPairsLatestResponse::getData)
				.map(CryptocurrencyMarketPairsLatestResponseData::getMarketPairs)
				.map(cryptocurrencyMarketPairs -> filterSingleExchangeSource(cryptocurrencyMarketPairs, currencyFilter))
				.map(pairs -> toCurrencyExchangePairDtoList(pairs, baseCurrency));
	}

	private List<CryptocurrencyMarketPair> filterSingleExchangeSource(
			List<CryptocurrencyMarketPair> cryptocurrencyMarketPairs, List<String> currencyFilter) {
		return cryptocurrencyMarketPairs.stream()
				.filter(this::isFromCurrentlyUsedExchange)
				.filter(currencyData -> filterCurrencyData(currencyFilter, currencyData))
				.collect(Collectors.toList());
	}

	private boolean filterCurrencyData(List<String> filter, CryptocurrencyMarketPair marketPair) {
		if (filter != null)
			return filter.contains(utilities.toQuoteCurrencySymbol(marketPair)) || filter.contains(utilities.toBaseCurrencySymbol(marketPair));
		else
			return true;
	}

	private List<CurrencyExchangePairDto> toCurrencyExchangePairDtoList(List<CryptocurrencyMarketPair> cryptocurrencyMarketPairs, String baseCurrency) {
		return cryptocurrencyMarketPairs.stream().map(pair -> toCurrencyExchangePairDto(pair, baseCurrency)).collect(Collectors.toList());
	}

	private CurrencyExchangePairDto toCurrencyExchangePairDto(CryptocurrencyMarketPair pair, String baseCurrency) {
		boolean isRetrievedPairReversed = !Objects.equals(baseCurrency, utilities.toBaseCurrencySymbol(pair));
		return CurrencyExchangePairDto.builder()
				.base(baseCurrency)
				.quote(isRetrievedPairReversed ? utilities.toBaseCurrencySymbol(pair) : utilities.toQuoteCurrencySymbol(pair))
				.rate(isRetrievedPairReversed ? calculateReversedRate(pair) : utilities.toPrice(pair))
				.build();
	}

	private double calculateReversedRate(CryptocurrencyMarketPair pair) {
		return 1.0 / utilities.toPrice(pair);
	}

	private boolean isFromCurrentlyUsedExchange(CryptocurrencyMarketPair currencyData) {
		return Objects.equals(exchangeName, utilities.toExchangeName(currencyData));
	}
}
