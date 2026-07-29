package com.flamingo.tictactoe.session.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Backs {@code SessionService.simulate()} with a dedicated thread pool so
 * playing out an automated game (several moves, each separated by
 * {@code simulation.move-delay-ms}) never ties up an HTTP request-handling
 * thread for the whole duration. {@code simulate()} hands the game-playing
 * loop to this executor and returns immediately; the UI already learns about
 * progress independently by polling {@code GET /sessions/{id}}.
 */
@Configuration
public class SimulationExecutorConfig {

    @Bean
    public TaskExecutor simulationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("game-simulation-");
        executor.initialize();
        return executor;
    }
}
