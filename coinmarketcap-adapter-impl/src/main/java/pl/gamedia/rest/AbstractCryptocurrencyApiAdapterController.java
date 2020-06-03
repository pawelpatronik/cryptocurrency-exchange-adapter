package pl.gamedia.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AbstractCryptocurrencyApiAdapterController {
	protected <T> ResponseEntity<T> toResponseEntity(T responseBody) {
		return new ResponseEntity<>(responseBody, HttpStatus.OK);
	}
}
