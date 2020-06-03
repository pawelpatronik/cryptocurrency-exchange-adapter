package pl.gamedia.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY,
		creatorVisibility = JsonAutoDetect.Visibility.ANY
)
public class CurrencyExchangePairDto {
	private String base;
	private String quote;
	private double rate;
}
