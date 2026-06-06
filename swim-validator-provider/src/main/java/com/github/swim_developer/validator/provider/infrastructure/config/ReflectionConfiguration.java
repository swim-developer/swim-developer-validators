package com.github.swim_developer.validator.provider.infrastructure.config;

import com.github.swim_developer.validator.provider.domain.model.AmqpConfig;
import com.github.swim_developer.validator.provider.domain.model.AssertionResult;
import com.github.swim_developer.validator.provider.domain.model.ConnectionResult;
import com.github.swim_developer.validator.provider.domain.model.ConsoleEntry;
import com.github.swim_developer.validator.provider.domain.model.TestOutcome;
import com.github.swim_developer.validator.provider.domain.model.TestResult;
import com.github.swim_developer.validator.provider.domain.model.TestScenario;
import com.github.swim_developer.validator.provider.domain.model.TestStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
        AmqpConfig.class,
        AssertionResult.class,
        ConnectionResult.class,
        ConsoleEntry.class,
        TestOutcome.class,
        TestResult.class,
        TestScenario.class,
        TestStatus.class
})
public class ReflectionConfiguration {
}
