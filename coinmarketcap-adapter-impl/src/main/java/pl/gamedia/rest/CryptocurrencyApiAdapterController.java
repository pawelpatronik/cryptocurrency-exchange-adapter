package pl.gamedia.rest;

import io.swagger.annotations.Api;
import io.vavr.control.Try;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.gamedia.boundary.api.CurrenciesApi;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.processor.GetExchangeForecastProcessor;
import pl.gamedia.processor.ListExchangeRatesProcessor;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping
@Api
public class CryptocurrencyApiAdapterController extends AbstractCryptocurrencyApiAdapterController implements CurrenciesApi {

	private final Executor asyncExecutor;
	private final ListExchangeRatesProcessor listExchangeRatesProcessor;
	private final GetExchangeForecastProcessor getExchangeForecastProcessor;

	public CryptocurrencyApiAdapterController(
			Executor asyncExecutor,
			ListExchangeRatesProcessor listExchangeRatesProcessor,
			GetExchangeForecastProcessor getExchangeForecastProcessor) {
		this.asyncExecutor = asyncExecutor;
		this.listExchangeRatesProcessor = listExchangeRatesProcessor;
		this.getExchangeForecastProcessor = getExchangeForecastProcessor;
	}

	@Override
	public CompletableFuture<ResponseEntity<ListExchangeRatesResponse>> currenciesCurrencyGet(String currency, @Valid List<String> filter) {
		return CompletableFuture.supplyAsync(
				() -> listExchangeRatesProcessor.process(currency, filter)
						.map(this::toResponseEntity)
						.get(),
				asyncExecutor);
	}

	@Override
	public CompletableFuture<ResponseEntity<CurrencyExchangeResponse>> currenciesExchangePost(@Valid CurrencyExchangeRequest currencyExchangeRequest) {
		return CompletableFuture.supplyAsync(
				() -> getExchangeForecastProcessor.process(currencyExchangeRequest)
						.map(this::toResponseEntity)
						.get(),
				asyncExecutor);
	}
}
