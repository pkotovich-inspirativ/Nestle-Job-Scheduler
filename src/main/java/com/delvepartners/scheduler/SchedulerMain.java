package com.delvepartners.scheduler;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang.SerializationUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.delvepartners.scheduler.Util.JOB_QUEUE_NAME;
import static com.delvepartners.scheduler.Util.MQ_URL_ENVVAR;
import static com.delvepartners.scheduler.Util.QUEUE_CONFIG;
import static com.delvepartners.scheduler.Util.getEnvOrThrow;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulerMain {

    final static Logger LOG = LoggerFactory.getLogger(SchedulerMain.class);
    final static ConnectionFactory factory = new ConnectionFactory();
    
    public static void main(String[] args) throws Exception {
        String queueUri = getEnvOrThrow(MQ_URL_ENVVAR);
        factory.setUri(queueUri);
        if(LOG.isInfoEnabled()) {
            LOG.info("queue connection factory configured for URI: "+queueUri);
        }

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        if(LOG.isInfoEnabled()) {
            LOG.info("created and started default scheduler");
        }

        /*
            CREATE TABLE job_configuration (
                id SERIAL PRIMARY KEY,
                name VARCHAR NOT NULL UNIQUE,
                description VARCHAR,
                project_name VARCHAR NOT NULL,
                job_name VARCHAR NOT NULL,
                version VARCHAR NOT NULL,
                class_name VARCHAR NOT NULL,
                schedule VARCHAR NOT NULL
            );
         */

        JobDetail jobDetail = newJob(TalendJobExecution.class)
                .usingJobData("projectName", "TEST_PROJECT")
                .usingJobData("jobName", "TestJob")
                .usingJobData("version", "1.0")
                .usingJobData("arguments", "a,b,c")
                .build();
        
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(repeatSecondlyForever(5))
                .build();

        if(LOG.isInfoEnabled()) {
            LOG.info("trigger scheduled to run job every 5 seconds. next run at: "+trigger.getNextFireTime());
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static class TalendJobExecution implements Job {
        private final static Logger LOG = LoggerFactory.getLogger(TalendJobExecution.class);

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            Connection connection = null;
            try {
                connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(JOB_QUEUE_NAME, true, false, false, QUEUE_CONFIG);

                JobDataMap map = jobExecutionContext.getMergedJobDataMap();
                Class<?> className = Util.getTalendJobClass(
                        map.getString("projectName"),
                        map.getString("jobName"),
                        map.getString("version")
                );
                String[] arguments = Util.asArray(map.getString("arguments"));
                TalendJobInfo jobInfo = new TalendJobInfo(className, arguments);

                byte[] body = SerializationUtils.serialize(jobInfo);
                channel.basicPublish("", JOB_QUEUE_NAME, MessageProperties.PERSISTENT_BASIC, body);

                if(LOG.isInfoEnabled()) {
                    LOG.info("message published to queue: "+ jobInfo);
                }

            } catch (IOException e) {
                LOG.error("caught IOException when executing job", e);
            } finally {
                if(null != connection) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        //noinspection ThrowFromFinallyBlock
                        throw new JobExecutionException(e);
                    }
                }
            }
        }
    }
}

