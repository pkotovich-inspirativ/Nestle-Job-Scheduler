package com.delvepartners.scheduler;

import static com.delvepartners.scheduler.Util.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerMain {
    final static Logger LOG = LoggerFactory.getLogger(WorkerMain.class);

    public static void main(String[] args) throws Exception {

        if(LOG.isInfoEnabled()) {
            LOG.info("starting up message queue.");
        }
        ConnectionFactory factory = new ConnectionFactory();
        String queueUri = getEnvOrThrow(MQ_URL_ENVVAR);
        factory.setUri(queueUri);
        Connection connection = factory.newConnection();

        if(LOG.isInfoEnabled()) {
            LOG.info("opened connection to queue at URI: "+queueUri);
        }
        Channel channel = connection.createChannel();

        channel.queueDeclare(JOB_QUEUE_NAME, true, false, false, QUEUE_CONFIG);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(JOB_QUEUE_NAME, false, consumer);
        if(LOG.isInfoEnabled()) {
            LOG.info("opened channel listening on queue: "+JOB_QUEUE_NAME);
        }
       
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(); 
            if (delivery != null) {
                Class<?> jobClass = (Class<?>) SerializationUtils.deserialize(delivery.getBody());

                if(LOG.isInfoEnabled()){
                    LOG.info("message received: " + jobClass.getSimpleName());
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                TalendJobRunner runner = new TalendJobRunner(jobClass);

                runner.execute();

                if(LOG.isInfoEnabled()) {
                    LOG.info("message processed");
                }
            }
        }
    }
}
