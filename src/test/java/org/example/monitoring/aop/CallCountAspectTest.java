package org.example.monitoring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallCountAspectTest {

    @Mock
    private JoinPoint joinPoint;

    private CallMeter callMeter;
    private CallCountAspect aspect;

    @BeforeEach
    void setUp() {
        callMeter = new CallMeter();
        aspect = new CallCountAspect(callMeter);
    }

    @Test
    void countExternalCall_incrementsFindByIdMeter() throws NoSuchMethodException {
        Method method = CountProbe.class.getMethod("findById");
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);

        aspect.countExternalCall(joinPoint);

        assertThat(callMeter.getFindByIdEntries()).isEqualTo(1);
    }

    static class CountProbe {

        @CountExternalCalls
        public void findById() {
        }
    }
}
