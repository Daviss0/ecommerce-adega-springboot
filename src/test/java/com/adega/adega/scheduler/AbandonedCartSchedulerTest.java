package com.adega.adega.scheduler;

import com.adega.adega.service.AbandonedCartService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbandonedCartSchedulerTest {

    @Test
    void shouldCallAbandonedCartServiceWhenSchedulerRuns() {

        AbandonedCartService abandonedCartService =
                mock(AbandonedCartService.class);

        when(abandonedCartService.clearAbandonedCarts())
                .thenReturn(2);

        AbandonedCartScheduler scheduler =
                new AbandonedCartScheduler(abandonedCartService);

        scheduler.schedule();

        verify(abandonedCartService)
                .clearAbandonedCarts();
    }
}
