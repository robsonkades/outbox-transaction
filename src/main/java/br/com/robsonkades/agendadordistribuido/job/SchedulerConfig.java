package br.com.robsonkades.agendadordistribuido.job;

import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.net.ssl.TrustManagerFactory;
import java.util.concurrent.ThreadFactory;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadFactory virtualThreadFactory = Thread
                .ofVirtual()
                .name("scheduler-vt-", 0)
                .factory();

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadFactory(virtualThreadFactory);
        scheduler.setPoolSize(10);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();



       // SslManagerBundle bundle = SslManagerBundle.from(tmf);

        return scheduler;
    }
}
