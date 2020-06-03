package pl.gamedia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ListExchangeRatesFailedException extends RuntimeException {
	public ListExchangeRatesFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
