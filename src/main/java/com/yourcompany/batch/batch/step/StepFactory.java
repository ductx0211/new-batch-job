package com.yourcompany.batch.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.yourcompany.batch.batch.tasklet.AbstractTasklet;

import java.util.LinkedHashMap;

@Component
public class StepFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public <I, O> Step getStep(Class<? extends AbstractStepBuilder<I, O>> stepBuilderClass) {
        return applicationContext.getBean(stepBuilderClass).build();
    }

    public <I, O> Step getStep(LinkedHashMap<String, Object> params, Class<? extends AbstractStepBuilder<I, O>> stepBuilderClass) {
        AbstractStepBuilder<I, O> step = applicationContext.getBean(stepBuilderClass);
        step.setParameters(params);
        return step.build();
    }

    public Step getStepTasklet(Class<? extends AbstractTasklet> taskletClass) {
        return applicationContext.getBean(taskletClass).buildStep();
    }

    public Step getStepTasklet(LinkedHashMap<String, Object> params, Class<? extends AbstractTasklet> taskletClass) {
        AbstractTasklet abstractTasklet = applicationContext.getBean(taskletClass);
        abstractTasklet.setParameters(params);
        return abstractTasklet.buildStep();
    }
}

