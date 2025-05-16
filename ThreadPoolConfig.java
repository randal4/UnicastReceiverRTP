import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public Executor rtpProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);  // Minimum number of threads
        executor.setMaxPoolSize(100);  // Maximum number of threads
        executor.setQueueCapacity(1000);  // Queue size before rejecting tasks
        executor.setThreadNamePrefix("RtpProcessing-");
        executor.initialize();
        return executor;
    }
}
