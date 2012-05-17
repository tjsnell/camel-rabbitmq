package org.apache.camel.component.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitMQClient {

    public QueueingConsumer getQueueingConsumer(Channel channel) throws InterruptedException {
        return new QueueingConsumer(channel);
    }

    public ConnectionFactory getConnectionFactory() throws IOException {
        return new ConnectionFactory();
    }
}
