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
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class PublishSubscribeTest extends CamelTestSupport {

    @EndpointInject(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @EndpointInject(uri = "mock:result2")
    private MockEndpoint result2;

    @Test
    public void noSubscription() throws Exception {
        result.expectedMessageCount(1);
        result2.expectedMessageCount(1);

        Exchange exchange = template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        assertEquals(1, result.getExchanges().size());
        assertEquals(1, result2.getExchanges().size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final String baseURI = String.format("rabbitmq://localhost?exchange=pubsubTestExchange&exchangeType=fanout");
                // two endpoints can't have the URL so just add the default port
                final String baseURI2 = String.format("rabbitmq://localhost:5672?exchange=pubsubTestExchange&exchangeType=fanout");

                // sub 1
                from(baseURI)
                    .to("mock:result");

                // sub 2
                from(baseURI2)
                    .to("mock:result2");

                // pub
                from("direct:start")
                    .to(baseURI);
            }
        };
    }
}
