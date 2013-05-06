package com.delvepartners.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.reflect.ConstructorUtils.invokeExactConstructor;
import static org.apache.commons.lang.reflect.MethodUtils.invokeMethod;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 4/6/13
 * Time: 4:11 PM
 */
public class TalendJobRunner {
    private static final Logger LOG = LoggerFactory.getLogger(TalendJobRunner.class);

    private static final String EXEC_METHOD="runJobInTOS";

    private final Class<?> jobClass;

    private final String[] arguments;

    @SuppressWarnings("unused")
    public TalendJobRunner(Class<?> jobClass, String ... args) {
        this.jobClass = jobClass;
        this.arguments = (args != null ? args : new String[]{});
    }

    public void execute() {
        if(LOG.isInfoEnabled()) {
            LOG.info("executing job: "+ jobClass.getSimpleName());
        }

        try {
            Object instance = invokeExactConstructor(
                    jobClass,
                    new Object[0]
            );

            if(LOG.isInfoEnabled()) {
                LOG.info(String.format("instance of %s created: %s", jobClass, instance.toString()));
            }

            Object result = invokeMethod(
                    instance,
                    EXEC_METHOD,
                    new Object[] {arguments},
                    new Class[]  {arguments.getClass()}
            );

            if(LOG.isInfoEnabled()) {
                LOG.info("job completed with resultCode: "+ result);
            }

        } catch (Exception e) {
            LOG.error("exception", e);
        }
    }
}
