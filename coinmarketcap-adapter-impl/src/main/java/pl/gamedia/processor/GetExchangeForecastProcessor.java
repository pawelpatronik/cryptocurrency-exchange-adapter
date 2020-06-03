package pl.gamedia.processor;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.exception.GetExchangeForecastFailedException;
import pl.gamedia.model.CurrencyExchangePairDto;
import pl.gamedia.provider.CryptocurrencyDataProvider;
import pl.gamedia.utils.Utilities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
@Slf4j
public class GetExchangeForecastProcessor {

	private static final String OPERATION_FAILURE_MESSAGE = "Process failed";

	private final double feeAmountPercentage;
	private final int parallelCalculationThreads;
	private final boolean isParallelCalculationEnabled;

	private final CryptocurrencyDataProvider cryptocurrencyDataProvider;
	private final Utilities utilities;


	public GetExchangeForecastProcessor(
			CryptocurrencyDataProvider cryptocurrencyDataProvider,
			Utilities utilities,
			@Value("${fee.amount.percentage}") Double feeAmountPercentage,
			@Value("${parallel.calculation.threads}") Integer parallelCalculationThreads,
			@Value("${parallel.calculation.enabled}") boolean isParallelCalculationEnabled) {
		this.cryptocurrencyDataProvider = cryptocurrencyDataProvider;
		this.utilities = utilities;
		this.feeAmountPercentage = feeAmountPercentage;
		this.parallelCalculationThreads = parallelCalculationThreads;
		this.isParallelCalculationEnabled = isParallelCalculationEnabled;
	}

	public Try<CurrencyExchangeResponse> process(CurrencyExchangeRequest request) {
		return cryptocurrencyDataProvider
				.getExchangePairList(request.getFrom(), utilities.toFilterSet(request.getTo()))
				.map(pairList -> validateRetrievedRatesResult(pairList, utilities.toFilterSet(request.getTo())))
				.map(exchangeRatesMap -> toCurrencyExchangeSummaryMap(exchangeRatesMap, request.getAmount()))
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

	private Map<String, CurrencyExchangeSummary> toCurrencyExchangeSummaryMap(List<CurrencyExchangePairDto> exchangePairDtos, Double amount) {
		if (isParallelCalculationEnabled) {
			ForkJoinPool pool = new ForkJoinPool(parallelCalculationThreads);
			return Try
					.of(() -> pool
							.submit(() -> parallelCalculateCurrencyExchangeSummaries(exchangePairDtos, amount))
							.get())
					.mapFailure(Case($(instanceOf(InterruptedException.class)), ex -> getParallelExecutionException(ex)))
					.mapFailure(Case($(instanceOf(ExecutionException.class)), ex -> getParallelExecutionException(ex)))
					.andFinally(pool::shutdown)
					.get();
		} else {
			return calculateCurrencyExchangeSummaries(exchangePairDtos, amount);
		}
	}

	private RuntimeException getParallelExecutionException(Throwable ex) {
		return new RuntimeException("Parallel execution failure: " + ex.getMessage(), ex);
	}

	private Map<String, CurrencyExchangeSummary> parallelCalculateCurrencyExchangeSummaries(List<CurrencyExchangePairDto> exchangePairDtos, Double amount) {
		return exchangePairDtos
				.parallelStream()
				.map(pairDto -> toCurrencyExchangeTuple(pairDto, amount))
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
	}

	private Map<String, CurrencyExchangeSummary> calculateCurrencyExchangeSummaries(List<CurrencyExchangePairDto> exchangePairDtos, Double amount) {
		return exchangePairDtos
				.stream()
				.map(pairDto -> toCurrencyExchangeTuple(pairDto, amount))
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
	}

	private Tuple2<String, CurrencyExchangeSummary> toCurrencyExchangeTuple(CurrencyExchangePairDto exchangePair, Double amount) {
		return new Tuple2<>(exchangePair.getQuote(), new CurrencyExchangeSummary()
				.amount(amount)
				.fee(calculateFee(amount))
				.rate(exchangePair.getRate())
				.result(calculateExchangeResult(exchangePair.getRate(), amount)));
	}

	private Double calculateExchangeResult(Double value, Double amount) {
		return value * amount * (100.0 - feeAmountPercentage) / 100.0;
	}

	private Double calculateFee(Double amount) {
		return amount * feeAmountPercentage / 100;
	}

	private List<CurrencyExchangePairDto> validateRetrievedRatesResult(List<CurrencyExchangePairDto> pairList, Set<String> filter) {
		if (!Objects.isNull(filter) && utilities.isBothSizeNotEqual(pairList, filter)) {
			throw new RuntimeException("Requested currency code could not be found in configured currency exchange");
		}
		return pairList;
	}
}
