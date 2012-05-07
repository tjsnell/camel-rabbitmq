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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RabbitMQ producer.
 */
// todo Send to a routingKey that doesn't exist so you get a published but not routed result add check for it

public class RabbitMQProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(RabbitMQProducer.class);
    private RabbitMQEndpoint endpoint;
    private Channel channel;


    public RabbitMQProducer(RabbitMQEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {

        String body = exchange.getIn().getBody(String.class);

        LOG.trace("Sending request [{}] from exchange [{}]...", body, exchange);

        RabitMQConfiguration config = endpoint.getConfiguration();
        createChannel();

        String routingKey = getRoutingKey(exchange.getIn(), config);
        System.out.println("routingKey = '" + routingKey + "'");

        configureChannel(config, channel);
        channel.basicPublish(config.getExchange(), routingKey, null, body.getBytes());

        Message message = getMessageForResponse(exchange);

        // todo set any headers?

        channel.close();
    }

    private void createChannel() throws Exception {
        Connection connection = endpoint.getConnection();
        RabitMQConfiguration configuration = endpoint.getConfiguration();

        channel = connection.createChannel();

        System.out.println("------------------------------------");
        System.out.println(configuration.toString());
        System.out.println("------------------------------------");
    }

    private void configureChannel(RabitMQConfiguration config, Channel channel) throws IOException {

        if (!config.getExchange().isEmpty()) {
            channel.exchangeDeclare(config.getExchange(), config.getExchangeType());
        } else {
            channel.queueDeclare(config.getQueue(), config.getDurable(), false, false, null);
        }
    }

    private String getRoutingKey(Message msg, RabitMQConfiguration config) {
        String key = config.getRoutingKey();

        String header = (String) msg.getHeader(RabbitMQConstants.ROUTING_KEY);

        if (config.getExchange().isEmpty()) {
            key = config.getQueue();
        }
        if (header != null) {
            if (!config.getExchange().isEmpty()) {
                key = header;
            } else {
                LOG.warn("No exchange set, ignoring routing key: " + header);
            }
        }
        return key;
    }

    private Message getMessageForResponse(Exchange exchange) {
        if (exchange.getPattern().isOutCapable()) {
            Message out = exchange.getOut();
            out.copyFrom(exchange.getIn());
            return out;
        }

        return exchange.getIn();
    }

}
