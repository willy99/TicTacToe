package com.flamingo.tictactoe.session.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A thread pool for running SessionService.simulate() in the background,
 * so playing out a whole automated game (several moves, each with a pause
 * of simulation.move-delay-ms) never blocks an HTTP request thread for
 * that whole time. simulate() hands the game-playing loop to this executor
 * and returns right away; the UI finds out about progress on its own by
 * polling GET /sessions/{id}.
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
