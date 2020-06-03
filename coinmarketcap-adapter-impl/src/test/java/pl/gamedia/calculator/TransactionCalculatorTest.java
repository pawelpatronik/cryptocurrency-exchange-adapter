package pl.gamedia.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import pl.gamedia.BaseTest;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.model.CurrencyExchangePairDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TransactionCalculatorTest extends BaseTest {

	private static final double FEE_AMOUNT_PERCENTAGE = 1;
	private static final int NUMBER_OF_ENTRIES = 1000000;

	@Test
	public void shouldPerformParallelCalculationsWithHighLoad() {
		// given
		Integer parallelCalculationThreads = 4;
		boolean isParallelCalculationEnabled = true;
		String amount = String.valueOf(1.0 + (10000000000.0 - 1.0) * new Random().nextDouble());
		List<CurrencyExchangePairDto> exchangePairs = IntStream
				.range(0, NUMBER_OF_ENTRIES)
				.mapToObj(i -> CurrencyExchangePairDto
						.builder()
						.rate(new Random().nextDouble())
						.quote("quote" + i)
						.base("base" + i)
						.build())
				.collect(Collectors.toList());

		// when
		long started = System.nanoTime();
		Map<String, CurrencyExchangeSummary> stringCurrencyExchangeSummaryMap = new TransactionCalculator(
				FEE_AMOUNT_PERCENTAGE, parallelCalculationThreads,
				isParallelCalculationEnabled).toCurrencyExchangeSummaryMap(exchangePairs, amount);
		log.info("Parallel calculations lapsed: " + (System.nanoTime() - started) / 1000000);

		// then
		assertNotNull(stringCurrencyExchangeSummaryMap);
	}

	@Test
	public void shouldPerformSingleThreadedCalculationsWithHighLoad() {
		// given
		Integer parallelCalculationThreads = 0;
		boolean isParallelCalculationEnabled = false;
		String amount = String.valueOf(1.0 + (10000000000.0 - 1.0) * new Random().nextDouble());
		List<CurrencyExchangePairDto> exchangePairs = IntStream
				.range(0, NUMBER_OF_ENTRIES)
				.mapToObj(i -> CurrencyExchangePairDto
						.builder()
						.rate(new Random().nextDouble())
						.quote("quote" + i)
						.base("base" + i)
						.build())
				.collect(Collectors.toList());

		// when
		long started = System.nanoTime();
		Map<String, CurrencyExchangeSummary> stringCurrencyExchangeSummaryMap = new TransactionCalculator(
				FEE_AMOUNT_PERCENTAGE, parallelCalculationThreads,
				isParallelCalculationEnabled).toCurrencyExchangeSummaryMap(exchangePairs, amount);
		log.info("Single thread calculations lapsed: " + (System.nanoTime() - started) / 1000000);

		// then
		assertNotNull(stringCurrencyExchangeSummaryMap);
	}

	@Test
	public void shouldPerformSingleThreadedCalculationsWithHighPrecision() {
		// given
		Integer parallelCalculationThreads = 0;
		boolean isParallelCalculationEnabled = false;
		String amount = String.valueOf(129.62309849823);
		String quote = "TSET";
		List<CurrencyExchangePairDto> exchangePairs =
				singletonList(CurrencyExchangePairDto.builder()
						.base("TEST")
						.quote(quote)
						.rate(0.129389843293819284)
						.build());

		// when
		Map<String, CurrencyExchangeSummary> stringCurrencyExchangeSummaryMap = new TransactionCalculator(
				FEE_AMOUNT_PERCENTAGE, parallelCalculationThreads,
				isParallelCalculationEnabled).toCurrencyExchangeSummaryMap(exchangePairs, amount);

		// then
		assertNotNull(stringCurrencyExchangeSummaryMap);
		assertEquals(
				new BigDecimal("16.604193277925828140508929675656"),
				new BigDecimal(stringCurrencyExchangeSummaryMap.get(quote).getResult()));
	}

}
