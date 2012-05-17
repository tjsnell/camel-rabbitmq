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

public class MessageDurabilityTest extends CamelTestSupport {
    @EndpointInject(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;


    @Test
    public void noSubscription() throws Exception {
        result.expectedMessageCount(1);

        Exchange exchange = template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertMockEndpointsSatisfied();

        assertEquals(1, result.getExchanges().size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final String baseURI = String.format("rabbitmq://localhost?queue=persistTestQueue&durable=true");

                from(baseURI + "&messageProperties=PERSISTENT_TEXT_PLAIN")
                    .to("mock:result");

                from("direct:start")
                    .to(baseURI);
            }
        };
    }
}
