package pl.gamedia.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig {

	public static final String THREAD_LOG_DESCRIPTION = "asyncThread-";

	@Value("${async.config.core.pool.size}")
	private Integer corePoolSize;

	@Value("${async.config.max.pool.size}")
	private Integer maxPoolSize;

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setThreadNamePrefix(THREAD_LOG_DESCRIPTION);
		return executor;
	}
}
