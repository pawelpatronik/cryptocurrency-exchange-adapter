package pl.gamedia.calculator;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.model.CurrencyExchangePairDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Component
public class TransactionCalculator {

	private final double feeAmountPercentage;
	private final int parallelCalculationThreads;
	private final boolean isParallelCalculationEnabled;

	public TransactionCalculator(
			@Value("${fee.amount.percentage}") Double feeAmountPercentage,
			@Value("${parallel.calculation.threads}") Integer parallelCalculationThreads,
			@Value("${parallel.calculation.enabled}") boolean isParallelCalculationEnabled) {
		this.feeAmountPercentage = feeAmountPercentage;
		this.parallelCalculationThreads = parallelCalculationThreads;
		this.isParallelCalculationEnabled = isParallelCalculationEnabled;
	}

	public Map<String, CurrencyExchangeSummary> toCurrencyExchangeSummaryMap(List<CurrencyExchangePairDto> exchangePairDtos, String amount) {
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

	private Map<String, CurrencyExchangeSummary> parallelCalculateCurrencyExchangeSummaries(List<CurrencyExchangePairDto> exchangePairDtos, String amount) {
		return exchangePairDtos
				.parallelStream()
				.map(pairDto -> toCurrencyExchangeTuple(pairDto, amount))
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
	}

	private Map<String, CurrencyExchangeSummary> calculateCurrencyExchangeSummaries(List<CurrencyExchangePairDto> exchangePairDtos, String amount) {
		return exchangePairDtos
				.stream()
				.map(pairDto -> toCurrencyExchangeTuple(pairDto, amount))
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
	}

	private Tuple2<String, CurrencyExchangeSummary> toCurrencyExchangeTuple(CurrencyExchangePairDto exchangePair, String amount) {
		return new Tuple2<>(exchangePair.getQuote(), new CurrencyExchangeSummary()
				.amount(amount)
				.fee(calculateFee(amount))
				.rate(String.valueOf(exchangePair.getRate()))
				.result(calculateExchangeResult(exchangePair.getRate(), amount)));
	}

	private String calculateExchangeResult(double value, String amount) {
		return BigDecimal.valueOf(value)
				.multiply(new BigDecimal(amount))
				.multiply(BigDecimal.valueOf((100.0 - feeAmountPercentage) / 100.0))
				.toPlainString();
	}

	private String calculateFee(String amount) {
		return new BigDecimal(amount)
				.multiply(BigDecimal.valueOf(feeAmountPercentage / 100))
				.toPlainString();
	}
}
