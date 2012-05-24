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
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a RabbitMQ endpoint.
 */
public class RabbitMQEndpoint extends DefaultEndpoint {
    private static final transient Logger LOG = LoggerFactory.getLogger(RabbitMQEndpoint.class);
    private RabbitMQConfiguration configuration;
    private Connection connection;
    private ConnectionFactory factory;
    private RabbitMQClient client;

    public RabbitMQEndpoint() {
    }

    public RabbitMQEndpoint(String uri, RabbitMQComponent component, RabbitMQConfiguration configuration) {
        super(uri, component);
        this.configuration = configuration;
    }


    @Override
    public Producer createProducer() throws Exception {
        return new RabbitMQProducer(this);
    }


    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        RabbitMQConsumer consumer = new RabbitMQConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public void doStop() throws Exception {
        super.doStop();
        if (connection.isOpen()) {
            connection.close();
        }
    }


    public QueueingConsumer getConsumer(Channel channel) throws InterruptedException {
        return getClient().getQueueingConsumer(channel);
    }


    public Exchange createExchange(Envelope envelope, AMQP.BasicProperties props, byte[] body) {
        Exchange exchange = new DefaultExchange(this, getExchangePattern());
        Message message = exchange.getIn();
        message.setBody(new String(body));

        setEnvelopeHeaders(envelope, message);
        setPropertiesHeaders(props, message);

        message.setHeader(RabbitMQConstants.ENDPOINT_ID, getId());

        CamelContext ctx = getCamelContext();

        for (Endpoint ep : ctx.getEndpoints()) {
            if (ep instanceof DefaultEndpoint) {
//                System.out.println("id: " + ((DefaultEndpoint)ep).getId());
            }
        }
        return exchange;

    }


    /**
     * Map the AMQP Properties to headers
     *
     * @param properties AMQP Properties
     * @param message    Message to set the headers on
     */
    private void setPropertiesHeaders(AMQP.BasicProperties properties, Message message) {
        setHeader(RabbitMQConstants.CONTENT_TYPE, properties.getContentType(), message);
        setHeader(RabbitMQConstants.CONTENT_ENCODING, properties.getContentEncoding(), message);
        setHeader(RabbitMQConstants.DELIVERY_MODE, properties.getDeliveryMode(), message);
        setHeader(RabbitMQConstants.PRIORITY, properties.getPriority(), message);
        setHeader(RabbitMQConstants.CORRELATION_ID, properties.getCorrelationId(), message);
        setHeader(RabbitMQConstants.REPLY_TO, properties.getReplyTo(), message);
        setHeader(RabbitMQConstants.EXPIRATION, properties.getExpiration(), message);
        setHeader(RabbitMQConstants.MESSAGE_ID, properties.getMessageId(), message);
        setHeader(RabbitMQConstants.TIMESTAMP, properties.getTimestamp(), message);
        setHeader(RabbitMQConstants.TYPE, properties.getType(), message);
        setHeader(RabbitMQConstants.USER_ID, properties.getUserId(), message);
        setHeader(RabbitMQConstants.APP_ID, properties.getAppId(), message);
        setHeader(RabbitMQConstants.CLUSTER_ID, properties.getClusterId(), message);
    }

    private void setHeader(String name, Object value, Message message) {
        if (value != null) {
            message.setHeader(name, value);
        }
    }

    private void setEnvelopeHeaders(Envelope envelope, Message message) {
        message.setHeader(RabbitMQConstants.DELIVERY_TAG, envelope.getDeliveryTag());
        message.setHeader(RabbitMQConstants.EXCHANGE, envelope.getExchange());
        message.setHeader(RabbitMQConstants.ROUTING_KEY, envelope.getRoutingKey());
    }

    public boolean isSingleton() {
        return true;
    }

    public RabbitMQConfiguration getConfiguration() {
        return configuration;
    }


    public RabbitMQClient getClient() {
        if (client == null) {
            client = getConfiguration().getRabbitMQClient() != null
                ? getConfiguration().getRabbitMQClient() : new RabbitMQClient();
        }
        return client;
    }

    public void setClient(RabbitMQClient client) {
        this.client = client;
    }

    @Override
    protected void doStart() throws Exception {
        if (connection == null) {
            createConnection();
        }


    }

    private void createConnection() throws IOException {

        factory = getClient().getConnectionFactory();

        factory.setHost(configuration.getHost());
        if (configuration.getUserName() != null) {
            factory.setUsername(configuration.getUserName());
        }
        if (configuration.getPassword() != null) {
            factory.setPassword(configuration.getPassword());
        }

        if (configuration.getVirtualHost() != null) {
            // todo if the vhost doesn't exist this throws an exception on newConnection
//            factory.setVirtualHost(configuration.getVirtualHost());
        }

        if (configuration.getPort() != 0) {
            factory.setPort(configuration.getPort());
        }

        connection = factory.newConnection();
    }


    public Connection getConnection() {
        return connection;
    }

    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }
}
