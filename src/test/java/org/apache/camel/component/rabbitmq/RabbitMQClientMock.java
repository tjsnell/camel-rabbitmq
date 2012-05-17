package org.apache.camel.component.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RabbitMQClientMock extends RabbitMQClient {
    private ConnectionFactory factory;
    private Channel channel;
    private Connection connection;
    private AMQP.Queue.DeclareOk declareOk;
    private QueueingConsumer queueingConsumer;
    private QueueingConsumer.Delivery delivery;

    @Override
    public QueueingConsumer getQueueingConsumer(Channel channel) throws InterruptedException {


        queueingConsumer = mock(QueueingConsumer.class);
        delivery = mock(QueueingConsumer.Delivery.class);

        when(queueingConsumer.nextDelivery(anyInt())).thenReturn(delivery).thenReturn(null);

        when(delivery.getBody()).thenReturn("This is my message text.".getBytes());
        when(delivery.getEnvelope()).thenReturn(new Envelope(1, false, "ex", "key"));
        when(delivery.getProperties()).thenReturn(new AMQP.BasicProperties());
        return queueingConsumer;
    }

    @Override
    public ConnectionFactory getConnectionFactory() throws IOException {

        factory = mock(ConnectionFactory.class);
        channel = mock(Channel.class);
        connection = mock(Connection.class);

        declareOk = mock(AMQP.Queue.DeclareOk.class);

        when(factory.newConnection()).thenReturn(connection);

        when(connection.createChannel()).thenReturn(channel);

        when(channel.queueDeclare()).thenReturn(declareOk);

        when(declareOk.getQueue()).thenReturn("queueOne");

        return factory;
    }
}
