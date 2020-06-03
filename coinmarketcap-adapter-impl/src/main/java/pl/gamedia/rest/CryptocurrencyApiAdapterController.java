package pl.gamedia.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.gamedia.boundary.api.CurrenciesApi;
import pl.gamedia.boundary.model.CurrencyExchangeRequest;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.ListExchangeRatesResponse;
import pl.gamedia.processor.GetExchangeForecastProcessor;
import pl.gamedia.processor.ListExchangeRatesProcessor;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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
	public CompletableFuture<ResponseEntity<ListExchangeRatesResponse>> currenciesCurrencyGet(
			@Size(min = 2)
			@ApiParam(value = "Mandatory parameter specifying a source platform of retrieved cryptocurrency exchange rates", required = true, defaultValue = "BTC")
			@PathVariable("currency") String currency,
			@ApiParam(value = "Optional parameter limiting list of returned entries to provided cryptocurrency codes")
			@Valid
			@RequestParam(value = "filter", required = false) List<String> filter) {
		return CompletableFuture.supplyAsync(
				() -> listExchangeRatesProcessor.process(currency, filter)
						.map(this::toResponseEntity)
						.get(),
				asyncExecutor);
	}

	@Override
	public CompletableFuture<ResponseEntity<CurrencyExchangeResponse>> currenciesExchangePost(
			@ApiParam(value = "Exchange forecast request body")
			@Valid
			@RequestBody(required = false)
					CurrencyExchangeRequest currencyExchangeRequest) {
		return CompletableFuture.supplyAsync(
				() -> getExchangeForecastProcessor.process(currencyExchangeRequest)
						.map(this::toResponseEntity)
						.get(),
				asyncExecutor);
	}
}
