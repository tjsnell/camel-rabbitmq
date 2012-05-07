package org.apache.camel.component.rabbitmq.sample;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Simp {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();


        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("rabbitmq:localhost?queue=camelQueue&prefetchCount=10").to("file://test");
//                from("timer://foo?fixedRate=true&period=6000").transform().simple("This is a test").to("rabbitmq:localhost/foo");
            }
        });
        ProducerTemplate template = context.createProducerTemplate();

        context.start();


        Thread.sleep(90000);
    }

}
