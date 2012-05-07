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

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
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
    private RabitMQConfiguration configuration;
    private Connection connection;
    private ConnectionFactory factory;

    public RabbitMQEndpoint() {
    }

    public RabbitMQEndpoint(String uri, RabbitMQComponent component, RabitMQConfiguration configuration) {
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

    public Exchange createExchange(RabbitMQMessage msg) {
        return createExchange(getExchangePattern(), msg);
    }

    private Exchange createExchange(ExchangePattern pattern, RabbitMQMessage msg) {
        Exchange exchange = new DefaultExchange(this, pattern);
        Message message = exchange.getIn();
        message.setBody(msg.getBody());

        return exchange;
    }

    public boolean isSingleton() {
        return true;
    }

    public RabitMQConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected void doStart() throws Exception {
        if (connection == null) {
            createConnection();
        }


    }

    private void createConnection() throws IOException {

        factory = new ConnectionFactory();

        factory.setHost(configuration.getHost());
        if (configuration.getUserName() != null) {
            factory.setUsername(configuration.getUserName());
        }
        if (configuration.getPassword() != null) {
            factory.setPassword(configuration.getPassword());
        }

        if (configuration.getVirtualHost() != null) {
            // todo this causes odd errors
            //     factory.setVirtualHost(configuration.getVirtualHost());
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
