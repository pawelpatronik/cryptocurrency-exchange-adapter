package pl.gamedia.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = {AsyncConfig.class})
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"async.config.core.pool.size=1",
		"async.config.max.pool.size=1"
})
public class AsyncConfigTest {

	@Autowired
	private Executor asyncExecutor;

	@Test
	public void shouldCreateAsyncExecutorBean() {
		// then
		assertNotNull(asyncExecutor);
	}
}
