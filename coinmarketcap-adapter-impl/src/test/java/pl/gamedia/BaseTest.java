package pl.gamedia;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import pl.gamedia.boundary.model.CurrencyExchangeResponse;
import pl.gamedia.boundary.model.CurrencyExchangeSummary;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Slf4j
public abstract class BaseTest {

	public static final double DOUBLE_COMPARISON_DELTA = 0.000000001;
	public static final String TEST_EXCHANGE_NAME = "BitMEX";

	protected static final String LIST_EXCHANGE_RATES_SAMPLE_RESPONSE_JSON_PATHNAME = "src/test/resources/list_exchange_rates_response/sample.json";
	protected static final String CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_JSON_PATHNAME = "src/test/resources/cryptocurrency_market_pairs_latest_response/sample.json";
	protected static final String CRYPTOCURRENCY_MARKET_PAIRS_LATEST_RESPONSE_ALL_JSON_PATHNAME = "src/test/resources/cryptocurrency_market_pairs_latest_response/all.json";
	protected static final String SAMPLE_CURRENCY_EXCHANGE_REQUEST_JSON_PATHNAME = "src/test/resources/currency_exchange_forecast_request/sample.json";
	protected static final String SAMPLE_CURRENCY_EXCHANGE_RESPONSE_JSON_PATHNAME = "src/test/resources/currency_exchange_forecast_response/sample.json";
	protected static final String TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME = "src/test/resources/currency_exchange_pair_list/test_data.json";
	protected static final String FILTERED_TEST_CURRENCY_EXCHANGE_PAIR_LIST_PATHNAME = "src/test/resources/currency_exchange_pair_list/test_data_filtered.json";
	protected static final String TEST_SUMMARY_MAP_PATHNAME = "src/test/resources/exchange_summary_map/test_data.json";

	protected final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true)
			.registerModule(new JavaTimeModule());

	protected <T> T load(String path, Class<T> requestClass) {
		return Try.of(() -> mapper.readValue(new File(path), requestClass))
				.onFailure(throwable -> log.info("Unable to perform test due to setup error: " + throwable.getMessage()))
				.get();
	}

	protected <T> T load(String path, TypeReference<T> typeReference) {
		return Try.of(() -> mapper.readValue(new File(path), typeReference))
				.onFailure(throwable -> log.info("Unable to perform test due to setup error: " + throwable.getMessage()))
				.get();
	}

	protected CurrencyExchangeSummary getExchangeSummary(CurrencyExchangeResponse response, String currency) {
		return response.getTo().get(currency);
	}

	protected void assertDoubleValue(Double amount, Double amount2) {
		assertEquals(amount,
				amount2, DOUBLE_COMPARISON_DELTA);
	}
}
