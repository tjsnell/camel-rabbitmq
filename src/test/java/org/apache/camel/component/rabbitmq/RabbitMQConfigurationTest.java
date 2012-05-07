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

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class RabbitMQConfigurationTest extends CamelTestSupport {

    private static String BASIC_URI = "rabbitmq://jpk:mypass@juicelabs.com:1234/camelVHost";

    @Test
    public void createSimplestConfiguration() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint("rabbitmq://localhost");

        RabitMQConfiguration config = endpoint.getConfiguration();

        assertEquals("localhost", config.getHost());
        assertEquals(-1, config.getPort());
    }

    @Test
    public void createEndpointWithBasicConfiguration() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint(BASIC_URI);


        RabitMQConfiguration config = endpoint.getConfiguration();
        checkBaseConfig(config);
    }

    @Test
    public void createEndpointWithQueue() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint(BASIC_URI + "?queue=myqueue");


        RabitMQConfiguration config = endpoint.getConfiguration();
        checkBaseConfig(config);

        assertEquals("myqueue", config.getQueue());
    }


    @Test
    public void createEndpointWithOneOption() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint(BASIC_URI + "?timeout=420");

        RabitMQConfiguration config = endpoint.getConfiguration();
        assertEquals(420, config.getTimeout());
        checkBaseConfig(config);
    }

    @Test
    public void createEndpointWithMultipleOptions() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint(BASIC_URI + "?timeout=420&requestedChannelMax=3&requestedFrameMax=4&requestedHeartbeat=5");

        RabitMQConfiguration config = endpoint.getConfiguration();
        assertEquals(420, config.getTimeout());
        assertEquals(3, config.getRequestedChannelMax());
        assertEquals(4, config.getRequestedFrameMax());
        assertEquals(5, config.getRequestedHeartbeat());
        checkBaseConfig(config);
    }

    @Test
    public void createEndpointWithMultipleOptions2() throws Exception {
        RabbitMQComponent component = new RabbitMQComponent(context);
        RabbitMQEndpoint endpoint = (RabbitMQEndpoint) component.createEndpoint(BASIC_URI + "?durable=true&autoDelete=true&prefetchCount=93");

        RabitMQConfiguration config = endpoint.getConfiguration();
        assertTrue(config.getDurable());
        assertTrue(config.getAutoDelete());
        assertEquals(93, config.getPrefetchCount());
        checkBaseConfig(config);
    }


    private void checkBaseConfig(RabitMQConfiguration config) {
        assertEquals("juicelabs.com", config.getHost());
        assertEquals("jpk", config.getUserName());
        assertEquals(1234, config.getPort());
        assertEquals("mypass", config.getPassword());
        assertEquals("camelVHost", config.getVirtualHost());
    }
}
