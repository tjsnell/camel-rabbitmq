/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.spi.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RabbitMQ consumer.
 */
public class RabbitMQConsumer extends DefaultConsumer {
    private static final transient Logger LOG = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final RabbitMQEndpoint endpoint;
    //    private Channel channel;
    private String queueName;
    private MyConsumer myConsumer;


    public RabbitMQConsumer(RabbitMQEndpoint endpoint, Processor processor) throws Exception {
        super(endpoint, processor);
        this.endpoint = endpoint;
        setupMQ();

        final RabbitMQConfiguration config = endpoint.getConfiguration();
        for (int i = 0; i < config.getConcurrentConsumers(); i++) {
            Runnable consumerThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        Channel channel = createChannel();
                        myConsumer = new MyConsumer(channel);
                        channel.basicConsume(queueName, config.isAutoAck(), myConsumer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(consumerThread);
            thread.start();
        }
    }

    private void setupMQ() throws Exception {
        createChannel();

        RabbitMQConfiguration config = endpoint.getConfiguration();

        System.out.println("========== Consumer ======================");
        System.out.println(config.toString());
        System.out.println("================================");

        Channel channel = createChannel();
        if (!config.getExchange().isEmpty()) {
            channel.exchangeDeclare(config.getExchange(), config.getExchangeType());
            queueName = channel.queueDeclare().getQueue();
            if (config.getBindingKeys().size() > 0) {
                setBindingKeys(config, channel);
            } else {
                channel.queueBind(queueName, config.getExchange(), "");
            }
        } else {
            queueName = config.getQueue();
            channel.queueDeclare(queueName, config.getDurable(), false, false, null);
        }
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    private void setBindingKeys(RabbitMQConfiguration config, Channel channel) throws IOException {
        for (String bindingKey : config.getBindingKeys()) {
            channel.queueBind(queueName, config.getExchange(), bindingKey);
        }
    }


    public class MyConsumer extends com.rabbitmq.client.DefaultConsumer {

        Channel channel;
        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        public MyConsumer(Channel channel) {
            super(channel);
            this.channel = channel;
        }

        @Override
        public void handleDelivery(String consumerTag,
                                   Envelope envelope,
                                   AMQP.BasicProperties properties,
                                   byte[] body) throws IOException {
            String routingKey = envelope.getRoutingKey();
            String contentType = properties.getContentType();
            long deliveryTag = envelope.getDeliveryTag();

            Exchange exchange = endpoint.createExchange(envelope, properties, body);

            exchange.addOnCompletion(new Completion(channel));
            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class Completion implements Synchronization {

        Channel channel;
        private Channel replyChannel;

        public Completion(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void onComplete(Exchange exchange) {
            try {
                Message in = exchange.getIn();
                long tag = in.getHeader(RabbitMQConstants.DELIVERY_TAG, Long.class);
                if (!endpoint.getConfiguration().isAutoAck()) {
                    try {
                        channel.basicAck(tag, false);
                    } catch (IOException e) {
                    }
                }

                String replyTo = exchange.getIn().getHeader(RabbitMQConstants.REPLY_TO, String.class);
                if (replyTo != null) {
                    String correlationId = exchange.getIn().getHeader(RabbitMQConstants.CORRELATION_ID, String.class);
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(correlationId)
                        .build();

                    if (replyChannel == null) {
                        replyChannel = createReplyChannel();
                    }
                    replyChannel.basicPublish("", replyTo, replyProps, "This is the response".getBytes());
                    replyChannel.basicAck(tag, false);
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(Exchange exchange) {
            System.out.println("^^^^^^^^^^^^^FAIL");
        }
    }


    /**
     * Strategy to delete the message after being processed.
     *
     * @param exchange the exchange
     */
    protected void processCommit(Exchange exchange) {

    }

    /**
     * Strategy when processing the exchange failed.
     *
     * @param exchange the exchange
     */
    protected void processRollback(Exchange exchange) {
        Exception cause = exchange.getException();
        if (cause != null) {
            LOG.warn("Exchange failed, so rolling back message status: " + exchange, cause);
        } else {
            LOG.warn("Exchange failed, so rolling back message status: {}", exchange);
        }
    }


    private Channel createReplyChannel() throws IOException {
        Connection connection = endpoint.getConnection();
        return connection.createChannel();
    }

    private Channel createChannel() throws IOException {
        Connection connection = endpoint.getConnection();
        RabbitMQConfiguration configuration = endpoint.getConfiguration();

        Channel channel = connection.createChannel();

        channel.basicQos(configuration.getPrefetch());
        return channel;
    }

}
