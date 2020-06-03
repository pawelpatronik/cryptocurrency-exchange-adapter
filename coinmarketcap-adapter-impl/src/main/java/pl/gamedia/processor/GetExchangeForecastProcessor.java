package pl.gamedia.processor;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPair;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponse;
import pl.gamedia.coinmarketcap.model.CryptocurrencyMarketPairsLatestResponseData;
import pl.gamedia.coinmarketcap.service.CryptocurrencyMarketPairsLatestService;
import pl.gamedia.exception.GetExchangeForecastFailedException;
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
import static java.util.Arrays.asList;

@Component
@Slf4j
public class GetExchangeForecastProcessor {

	private static final String OPERATION_FAILURE_MESSAGE = "Process failed";

	private final Double feeAmountPercentage;

	private final CryptocurrencyDataProvider cryptocurrencyDataProvider;
	private final Utilities utilities;


	public GetExchangeForecastProcessor(
			CryptocurrencyDataProvider cryptocurrencyDataProvider,
			Utilities utilities,
			@Value("${fee.amount.percentage}") Double feeAmountPercentage) {
		this.cryptocurrencyDataProvider = cryptocurrencyDataProvider;
		this.utilities = utilities;
		this.feeAmountPercentage = feeAmountPercentage;
	}

	public Try<CurrencyExchangeResponse> process(CurrencyExchangeRequest request) {
		return cryptocurrencyDataProvider
				.getExchangePairList(request.getFrom(), toCurrencyFilter(request))
				.map(pairList -> validateRetrievedRatesResult(pairList, toCurrencyFilter(request)))
				.map(exchangeRatesMap -> toCurrencyExchangeSummaryMap(exchangeRatesMap, request.getAmount()))
				.map(summaryMap -> new CurrencyExchangeResponse()
						.from(request.getFrom())
						.currencyB(summaryMap.get(request.getTo().getCurrencyB()))
						.currencyC(summaryMap.get(request.getTo().getCurrencyC())))
				.mapFailure(Case($(instanceOf(Exception.class)),
						throwable -> new GetExchangeForecastFailedException(OPERATION_FAILURE_MESSAGE, throwable)));
	}

	private List<String> toCurrencyFilter(CurrencyExchangeRequest request) {
		return asList(request.getTo().getCurrencyB(), request.getTo().getCurrencyC());
	}

	private Map<String, CurrencyExchangeSummary> toCurrencyExchangeSummaryMap(List<CurrencyExchangePairDto> exchangePairDtos, Double amount) {
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

	private List<CurrencyExchangePairDto> validateRetrievedRatesResult(List<CurrencyExchangePairDto> pairList, List<String> filter) {
		if (!Objects.isNull(filter) && utilities.isBothSizeNotEqual(pairList, filter)) {
			throw new RuntimeException("Requested currency code could not be found in configured currency exchange");
		}
		return pairList;
	}
}
