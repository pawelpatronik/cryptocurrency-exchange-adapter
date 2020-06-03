package pl.gamedia.processor;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPair;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;
import pl.gamedia.exception.ListExchangeRatesFailedException;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@Slf4j
public class ListExchangeRatesProcessor {

	private static final String OPERATION_FAILURE_MESSAGE = "Process failed";

	private final CryptocurrencyDataProvider cryptocurrencyDataProvider;
	private final Utilities utilities;

	public ListExchangeRatesProcessor(
			CryptocurrencyDataProvider cryptocurrencyDataProvider,
			Utilities utilities) {
		this.cryptocurrencyDataProvider = cryptocurrencyDataProvider;
		this.utilities = utilities;
	}

	public Try<ListExchangeRatesResponse> process(final String sourceCurrency, final List<String> filter) {
		return cryptocurrencyDataProvider
				.getExchangePairList(sourceCurrency, filter)
				.map(utilities::toRatesMap)
				.map(ratesMap -> validateRetrievedRatesResult(ratesMap, filter))
				.map(ratesMap -> new ListExchangeRatesResponse()
						.source(sourceCurrency)
						.rates(ratesMap))
				.mapFailure(Case($(instanceOf(Exception.class)),
						throwable -> new ListExchangeRatesFailedException(OPERATION_FAILURE_MESSAGE, throwable)));
	}

	private Map<String, Double> validateRetrievedRatesResult(Map<String, Double> resultMap, List<String> filter) {
		if (!Objects.isNull(filter) && utilities.isBothSizeNotEqual(resultMap, filter)) {
			throw new RuntimeException("Requested currency code could not be found in configured currency exchange");
		}
		return resultMap;
	}
}
