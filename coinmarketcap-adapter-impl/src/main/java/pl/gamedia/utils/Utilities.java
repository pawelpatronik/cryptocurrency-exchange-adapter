package pl.gamedia.utils;

import org.springframework.stereotype.Component;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPair;
import pl.gamedia.model.CurrencyExchangePairDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Utilities {

	public boolean isBothSizeNotEqual(Map<?, ?> resultMap, List<?> filter) {
		return resultMap.size() != filter.size();
	}

	public boolean isBothSizeNotEqual(List<?> resultList, List<?> filter) {
		return resultList.size() != filter.size();
	}

	public String toExchangeName(CryptocurrencyMarketPair currencyData) {
		return currencyData.getExchange().getName();
	}

	public Double toPrice(CryptocurrencyMarketPair pair) {
		return pair.getQuote().getExchangeReported().getPrice();
	}

	public String toQuoteCurrencySymbol(CryptocurrencyMarketPair pair) {
		return pair.getMarketPairQuote().getCurrencySymbol();
	}

	public String toBaseCurrencySymbol(CryptocurrencyMarketPair pair) {
		return pair.getMarketPairBase().getCurrencySymbol();
	}

	public Map<String, Double> toRatesMap(List<CurrencyExchangePairDto> pairs) {
		return pairs
				.stream()
				.collect(Collectors.toMap(
						CurrencyExchangePairDto::getQuote,
						CurrencyExchangePairDto::getRate));
	}

}
