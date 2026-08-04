package hr.asimr.radegast.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMetrics {

    private static final String METRIC_NAME = "radegast.authentication.attempts";

    private final Counter successfulLoginCounter;
    private final Counter failedLoginCounter;

    public AuthenticationMetrics(MeterRegistry meterRegistry) {
        successfulLoginCounter = Counter.builder(METRIC_NAME)
                .description("Number of successful login attempts")
                .tag("outcome", "success")
                .register(meterRegistry);

        failedLoginCounter = Counter.builder(METRIC_NAME)
                .description("Number of failed login attempts")
                .tag("outcome", "failure")
                .register(meterRegistry);
    }

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        successfulLoginCounter.increment();
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        failedLoginCounter.increment();
    }
}
