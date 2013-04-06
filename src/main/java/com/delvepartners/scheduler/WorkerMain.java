package com.delvepartners.scheduler;

import static com.delvepartners.scheduler.Util.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x-ha-policy", "all");
        channel.queueDeclare(JOB_QUEUE_NAME, true, false, false, params);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(JOB_QUEUE_NAME, false, consumer);
        if(LOG.isInfoEnabled()) {
            LOG.info("opened channel listening on queue: "+JOB_QUEUE_NAME);
        }
       
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(); 
            if (delivery != null) {
                String msg = new String(delivery.getBody(), DEFAULT_ENCODING);
                if(LOG.isInfoEnabled()){
                    LOG.info("message received: " + msg);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        }

    }

}
