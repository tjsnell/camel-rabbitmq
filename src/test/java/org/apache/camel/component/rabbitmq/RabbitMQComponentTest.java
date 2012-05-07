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

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class RabbitMQComponentTest extends CamelTestSupport {

    @EndpointInject(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @Test
    public void sendInOnly() throws Exception {
        result.expectedMessageCount(1);

        Exchange exchange = template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        Exchange resultExchange = result.getExchanges().get(0);
        assertEquals("This is my message text.", resultExchange.getIn().getBody());

        assertEquals("This is my message text.", exchange.getIn().getBody());
   }

    @Test
    public void sendInOut() throws Exception {
        result.reset();
        result.expectedMessageCount(1);

        Exchange exchange = template.send("direct:start", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my in/out message text.");
            }
        });

        assertMockEndpointsSatisfied();

        Exchange resultExchange = result.getExchanges().get(0);
        assertEquals("This is my in/out message text.", resultExchange.getIn().getBody());

        assertEquals("This is my in/out message text.", exchange.getOut().getBody());

    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        return registry;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            final String sqsURI = String.format("rabbitmq://localhost?queue=camelQueue&exchange=camelExchange&routingKey=camelKey&exchangeType=fanout");
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .to(sqsURI);

                from(sqsURI)
                    .to("mock:result");
            }
        };
    }
}
