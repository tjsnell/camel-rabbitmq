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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.CastUtils;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RabbitMQ consumer.
 */
public class RabbitMQConsumer extends ScheduledBatchPollingConsumer {
    private static final transient Logger LOG = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final RabbitMQEndpoint endpoint;
    private Channel channel;
    private QueueingConsumer consumer;
    private String queueName;
    private boolean rpc;
    private Channel replyChannel;

    public RabbitMQConsumer(RabbitMQEndpoint endpoint, Processor processor) throws Exception {
        super(endpoint, processor);
        this.endpoint = endpoint;
        setupMQ();
        // todo
        rpc = true;
    }

    private void setupMQ() throws Exception {
        createChannel();

        RabitMQConfiguration config = endpoint.getConfiguration();

        System.out.println("========== Consumer ======================");
        System.out.println(config.toString());
        System.out.println("================================");

        if (!config.getExchange().isEmpty()) {
            channel.exchangeDeclare(config.getExchange(), config.getExchangeType());
            queueName = channel.queueDeclare().getQueue();
            if (config.getBindingKeys().size() > 0) {
                setBindingKeys(config);
            } else {
                channel.queueBind(queueName, config.getExchange(), "");
            }
        } else {
            queueName = config.getQueue();
            channel.queueDeclare(queueName, config.getDurable(), false, false, null);
        }
        consumer = endpoint.getConsumer(channel);
    }

    private void setBindingKeys(RabitMQConfiguration config) throws IOException {
        for (String bindingKey : config.getBindingKeys()) {
            channel.queueBind(queueName, config.getExchange(), bindingKey);
        }
    }

    @Override
    protected int poll() throws Exception {
        channel.basicConsume(queueName, endpoint.getConfiguration().isAutoAck(), consumer);
        List<RabbitMQMessage> messages = consumeMessages(consumer);
        LOG.trace("Received {} messages", messages.size());

        Queue<Exchange> exchanges = createExchanges(messages);
        // todo  channel.close();
        return processBatch(CastUtils.cast(exchanges));
    }

    private List<RabbitMQMessage> consumeMessages(QueueingConsumer consumer) throws InterruptedException {
        List<RabbitMQMessage> messages = new ArrayList<RabbitMQMessage>();

        QueueingConsumer.Delivery delivery = consumer.nextDelivery(50);
        while (delivery != null) {
            RabbitMQMessage message = new RabbitMQMessage();
            message.setBody(new String(delivery.getBody()));
            message.setEnvelope(delivery.getEnvelope());
            message.setProperties(delivery.getProperties());
            messages.add(message);
            delivery = consumer.nextDelivery(50);
        }
        return messages;
    }

    @Override
    public int processBatch(Queue<Object> exchanges) throws Exception {
        int total = exchanges.size();

        for (int index = 0; index < total && isBatchAllowed(); index++) {
            // only loop if we are started (allowed to run)
            Exchange exchange = ObjectHelper.cast(Exchange.class, exchanges.poll());
            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // update pending number of exchanges
            pendingExchanges = total - index - 1;

            // add on completion to handle after work when the exchange is done
            exchange.addOnCompletion(new Synch());

            LOG.trace("Processing exchange [{}]...", exchange);

            getProcessor().process(exchange);
        }

        return total;
    }

    private class Synch implements Synchronization {

        @Override
        public void onComplete(Exchange exchange) {
            try {
                System.out.println("***************************************************************");
                long tag = exchange.getIn().getHeader(RabbitMQConstants.DELIVERY_TAG, Long.class);
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

                    System.out.println(")()()()()()()()( Sending Reply to: " + replyTo);
                    if (replyChannel == null) {
                        replyChannel = createReplyChannel();
                    }
                    replyChannel.basicPublish("", replyTo, replyProps, "This is the response".getBytes());
                    replyChannel.basicAck(tag, false);
                }
            } catch (Exception e) {
            }

        }

        @Override
        public void onFailure(Exchange exchange) {

        }
    }


    private Queue<Exchange> createExchanges(List<RabbitMQMessage> messages) {
        LOG.trace("Received {} messages in this poll", messages.size());

        Queue<Exchange> answer = new LinkedList<Exchange>();
        for (RabbitMQMessage message : messages) {
            Exchange exchange = endpoint.createExchange(message);
            answer.add(exchange);
        }

        return answer;
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

    private void createChannel() throws IOException {
        Connection connection = endpoint.getConnection();
        RabitMQConfiguration configuration = endpoint.getConfiguration();

        channel = connection.createChannel();

        channel.basicQos(configuration.getPrefetch());
    }

}
