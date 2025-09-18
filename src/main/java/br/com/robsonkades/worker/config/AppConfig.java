package br.com.robsonkades.worker.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AppConfig {

    @Bean("cpuBoundExecutor")
    public ThreadPoolTaskExecutor cpuBoundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // número de cores
        executor.setMaxPoolSize(4);  // limite máximo
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("CPU-POOL-");
        executor.setThreadPriority(Thread.MAX_PRIORITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setStrictEarlyShutdown(true);
        executor.setPrestartAllCoreThreads(true);
        executor.initialize();
        return executor;
    }

    // ---------------- I/O-bound Async Executor (Virtual Threads) ----------------
    @Bean("ioBoundExecutor")
    public SimpleAsyncTaskExecutor ioBoundExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("VTHREAD-");
        executor.setVirtualThreads(true);      // virtual threads
        executor.setConcurrencyLimit(1000);      // limite lógico
        executor.setRejectTasksWhenLimitReached(true);
        executor.setTaskTerminationTimeout(Duration.ofSeconds(5).toMillis());
        return executor;
    }

    // ---------------- Scheduled Task Scheduler ----------------
    @Bean("scheduledExecutor")
    public ThreadPoolTaskScheduler scheduledExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);              // número de threads para agendamento
        scheduler.setThreadNamePrefix("SCHED-");
        scheduler.setThreadPriority(Thread.NORM_PRIORITY);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduler.setAcceptTasksAfterContextClose(false);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();
        return scheduler;
    }

    @Bean("fastScheduler")
    public ThreadPoolTaskScheduler fastScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("FAST-SCHED-");
        scheduler.setVirtualThreads(true); // threads virtuais
        scheduler.setThreadPriority(Thread.NORM_PRIORITY);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduler.setAcceptTasksAfterContextClose(false);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
