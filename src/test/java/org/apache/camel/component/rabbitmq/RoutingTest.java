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

public class RoutingTest extends CamelTestSupport {

    @EndpointInject(uri = "direct:start_error")
    private ProducerTemplate templateError;

    @EndpointInject(uri = "direct:start_warn")
    private ProducerTemplate templateWarn;

    @EndpointInject(uri = "mock:resultErrorWarn")
    private MockEndpoint resultErrorWarn;

    @EndpointInject(uri = "mock:resultWarn")
    private MockEndpoint resultWarn;

    @EndpointInject(uri = "mock:resultAll")
    private MockEndpoint resultNo;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @Test
    public void errorSubscription() throws Exception {
        result.expectedMessageCount(1);

        Exchange exchange = templateError.send("direct:start_error", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        Exchange resultExchange = result.getExchanges().get(0);
        assertEquals("This is my message text.", resultExchange.getIn().getBody());
    }


    @Test
    public void errorAndWarnSubscription() throws Exception {
        resultErrorWarn.expectedMessageCount(1);

        Exchange exchange = templateError.send("direct:start_error", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        Exchange resultExchange = resultErrorWarn.getExchanges().get(0);
        assertEquals("This is my message text.", resultExchange.getIn().getBody());
    }


    @Test
    public void warnSubscription() throws Exception {
        resultWarn.expectedMessageCount(1);

        Exchange exchange = templateWarn.send("direct:start_warn", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        Exchange resultExchange = resultWarn.getExchanges().get(0);
        assertEquals("This is my message text.", resultExchange.getIn().getBody());
    }

    @Test
    public void noSubscription() throws Exception {
        resultNo.expectedMessageCount(0);

        Exchange exchange = templateWarn.send("direct:start_warn", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        assertEquals(0,resultNo.getExchanges().size());
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            final String baseURI = String.format("rabbitmq://localhost?exchange=routingTestExchange");

            @Override
            public void configure() throws Exception {

                from(baseURI + "&bindingKey=error&exchangeType=direct")
                    .to("mock:result");

                from(baseURI + "&bindingKey=error,warn&exchangeType=direct")
                    .to("mock:resultErrorWarn");

                from(baseURI + "&bindingKey=warn&exchangeType=direct")
                    .to("mock:resultWarn");

                from(baseURI + "&exchangeType=direct")
                    .to("mock:resultNo");

                from("direct:start_error")
                    .to(baseURI + "&routingKey=error&exchangeType=direct");

                from("direct:start_warn")
                    .to(baseURI + "&routingKey=warn&exchangeType=direct");
            }
        };
    }

}
