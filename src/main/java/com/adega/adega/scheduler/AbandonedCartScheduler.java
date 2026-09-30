package com.adega.adega.scheduler;


import com.adega.adega.service.AbandonedCartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AbandonedCartScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AbandonedCartScheduler.class);

    private final AbandonedCartService abandonedCartService;

    public AbandonedCartScheduler(AbandonedCartService abandonedCartService) {
       this.abandonedCartService = abandonedCartService;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Sao_Paulo")
    public void schedule() {
        int cleanedCarts = abandonedCartService.clearAbandonedCarts();

        logger.info("Limpeza de carrinhos abandonados concluída. Carrinhos limpos: {}", cleanedCarts);    }
}
