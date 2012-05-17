package org.apache.camel.component.rabbitmq;

import com.rabbitmq.client.BasicProperties;

public interface RabbitMQConstants {
    String ROUTING_KEY = "RabbitMQRoutingKey";
    String RECEIPT_HANDLE = "RabbitMQReceiptHandle";
    String PROPERTIES = "RabbitMQProperties";
    String DELIVERY_TAG = "RabbitMQDeliveryTag";
    String EXCHANGE = "RabbitMQExchange";
    String CONTENT_TYPE = "RabbitMQContentType";
    String CONTENT_ENCODING = "RabbitMQContentEncoding";
    // todo ? String HEADERS = "RabbitMQ";
    String DELIVERY_MODE = "RabbitMQDeliveryMode";
    String PRIORITY = "RabbitMQPriority";
    String CORRELATION_ID = "RabbitMQCorrelationID";
    String REPLY_TO = "RabbitMQReplyTo";
    String EXPIRATION = "RabbitMQExpiration";
    String MESSAGE_ID = "RabbitMQMessageID";
    String TIMESTAMP = "RabbitMQTimeStamp";
    String TYPE = "RabbitMQType";
    String USER_ID = "RabbitMQUserID";
    String APP_ID = "RabbitMQAppID";
    String CLUSTER_ID = "RabbitMQClusterID";
    String ENDPOINT_ID = "RabbitMQEndpointID";

    public enum MessageProperties {
        BASIC(com.rabbitmq.client.MessageProperties.BASIC),
        MINIMAL_BASIC(com.rabbitmq.client.MessageProperties.MINIMAL_BASIC),
        MINIMAL_PERSISTENT_BASIC(com.rabbitmq.client.MessageProperties.MINIMAL_PERSISTENT_BASIC),
        PERSISTENT_BASIC(com.rabbitmq.client.MessageProperties.PERSISTENT_BASIC),
        PERSISTENT_TEXT_PLAIN(com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN),
        TEXT_PLAIN(com.rabbitmq.client.MessageProperties.TEXT_PLAIN);


        private String name;
        BasicProperties property;


        private MessageProperties(BasicProperties property) {
            this.property = property;
        }

        public BasicProperties getProperty() {
            return property;
        }
    }
}
