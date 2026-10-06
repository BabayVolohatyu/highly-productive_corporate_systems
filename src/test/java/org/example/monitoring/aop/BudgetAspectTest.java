package org.example.monitoring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    private MutableClock clock;
    private OperationTimings operationTimings;
    private BudgetAspect aspect;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(1000L);
        operationTimings = new OperationTimings();
        aspect = new BudgetAspect(clock, operationTimings);
    }

    @Test
    void nonPositiveBudget_doesNotProceedAndThrows() throws Throwable {
        stubJoinPoint(BudgetProbe.class.getMethod("zeroBudget"));

        assertThatThrownBy(() -> aspect.enforceBudget(joinPoint))
                .isInstanceOf(BudgetExceededException.class);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void withinBudget_proceedsOnceAndReturnsValue() throws Throwable {
        stubJoinPoint(BudgetProbe.class.getMethod("withinBudget"));
        when(joinPoint.proceed()).thenReturn("ok");
        clock.setMillis(1000L, 1050L);

        Object result = aspect.enforceBudget(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void elapsedOverBudget_proceedsThenThrows() throws Throwable {
        stubJoinPoint(BudgetProbe.class.getMethod("withinBudget"));
        when(joinPoint.proceed()).thenReturn("late");
        clock.setMillis(1000L, 2000L);

        assertThatThrownBy(() -> aspect.enforceBudget(joinPoint))
                .isInstanceOf(BudgetExceededException.class)
                .hasMessageContaining("1000ms");
        verify(joinPoint).proceed();
    }

    private void stubJoinPoint(Method method) {
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
    }

    static class BudgetProbe {

        @WithinBudget(maxMillis = 0)
        public void zeroBudget() {
        }

        @WithinBudget(maxMillis = 100)
        public void withinBudget() {
        }
    }

    static final class MutableClock extends Clock {

        private long[] sequence;
        private int index;

        MutableClock(long... millisSequence) {
            this.sequence = millisSequence;
        }

        void setMillis(long... millisSequence) {
            this.sequence = millisSequence;
            this.index = 0;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }

        @Override
        public long millis() {
            if (index >= sequence.length) {
                return sequence[sequence.length - 1];
            }
            return sequence[index++];
        }
    }
}
