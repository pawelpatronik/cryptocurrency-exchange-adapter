package pl.gamedia.processor;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.calculator.TransactionCalculator;
import pl.gamedia.exception.GetExchangeForecastFailedException;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@Slf4j
public class GetExchangeForecastProcessor {

	private static final String OPERATION_FAILURE_MESSAGE = "Process failed";

	private final CryptocurrencyDataProvider cryptocurrencyDataProvider;
	private final TransactionCalculator transactionCalculator;
	private final Utilities utilities;

	public GetExchangeForecastProcessor(
			CryptocurrencyDataProvider cryptocurrencyDataProvider,
			TransactionCalculator transactionCalculator, Utilities utilities) {
		this.cryptocurrencyDataProvider = cryptocurrencyDataProvider;
		this.transactionCalculator = transactionCalculator;
		this.utilities = utilities;
	}

	public Try<CurrencyExchangeResponse> process(CurrencyExchangeRequest request) {
		return cryptocurrencyDataProvider
				.getExchangePairList(request.getFrom(), utilities.toFilterSet(request.getTo()))
				.map(pairList -> validateRetrievedRatesResult(pairList, utilities.toFilterSet(request.getTo())))
				.map(exchangeRatesMap -> transactionCalculator.toCurrencyExchangeSummaryMap(exchangeRatesMap, request.getAmount()))
				.map(summaryMap -> toCurrencyExchangeResponse(request, summaryMap))
				.mapFailure(Case($(instanceOf(Exception.class)),
						throwable -> new GetExchangeForecastFailedException(OPERATION_FAILURE_MESSAGE, throwable)));
	}

	private CurrencyExchangeResponse toCurrencyExchangeResponse(CurrencyExchangeRequest request, Map<String, CurrencyExchangeSummary> summaryMap) {
		return new CurrencyExchangeResponse()
				.from(request.getFrom())
				.to(utilities.toFilterSet(request.getTo())
						.stream()
						.map(toCurrency -> new Tuple2<>(toCurrency, summaryMap.get(toCurrency)))
						.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2)));
	}

	private List<CurrencyExchangePairDto> validateRetrievedRatesResult(List<CurrencyExchangePairDto> pairList, Set<String> filter) {
		if (!Objects.isNull(filter) && utilities.isBothSizeNotEqual(pairList, filter)) {
			throw new RuntimeException("Requested currency code could not be found in configured currency exchange");
		}
		return pairList;
	}
}
