package demo.controllers;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsernameController {

    @Value("${app.topic.request_topic}")
    private String REQUEST_TOPIC;

    @Value("${app.topic.reply_topic}")
    private String REPLY_TOPIC;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsernameController.class);

    private static final String MESSAGE_SENT_TEMPLATE = "*************************************************************\n"
            + "Message sent and acknowledged by the server with metadata: {}"
            + "\n*************************************************************";

    private static final String REPLY_RECEIVED_TEMPLATE = "*************************************************************\n"
            + "Reply received with value: {}"
            + "\n*************************************************************";

    private final ReplyingKafkaTemplate<String, String, String> template;

    @Autowired
    public UsernameController(final ReplyingKafkaTemplate<String, String, String> template) {
        this.template = template;
    }

    @GetMapping("/users")
    public String sendRequest(@RequestParam(value = "username", defaultValue = "anonymous") String username) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(REQUEST_TOPIC, username);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, REPLY_TOPIC.getBytes()));

        RequestReplyFuture<String, String, String> replyFuture = template.sendAndReceive(record);

        SendResult<String, String> sendResult = replyFuture.getSendFuture().get();
        LOGGER.debug(MESSAGE_SENT_TEMPLATE, sendResult.getRecordMetadata());

        ConsumerRecord<String, String> consumerRecord = replyFuture.get();
        LOGGER.debug(REPLY_RECEIVED_TEMPLATE, consumerRecord.value());
        return "Request Sent.";
    }
}
