package com.delvepartners.scheduler;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.delvepartners.scheduler.Util.DEFAULT_ENCODING;
import static com.delvepartners.scheduler.Util.JOB_QUEUE_NAME;
import static com.delvepartners.scheduler.Util.MQ_URL_ENVVAR;
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

        JobDetail jobDetail = newJob(HelloJob.class).build();
        
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(repeatSecondlyForever(5))
                .build();

        if(LOG.isInfoEnabled()) {
            LOG.info("trigger scheduled to run job every 5 seconds: "+trigger.getDescription());
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static class HelloJob implements Job {
        
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("x-ha-policy", "all");
                channel.queueDeclare(JOB_QUEUE_NAME, true, false, false, params);

                String mainClass = "test_project.testjob_1_0.TestJob";
                byte[] body = mainClass.getBytes(DEFAULT_ENCODING);
                channel.basicPublish("", JOB_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, body);

                if(LOG.isInfoEnabled()) {
                    LOG.info("message published to queue: "+ mainClass);
                }

                connection.close();
            }
            catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

        }
        
    }

}
