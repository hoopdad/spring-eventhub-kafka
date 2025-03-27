//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.

// Adapted for current MI usage and container deployment from https://github.com/Azure/azure-event-hubs-for-kafka/tree/master/tutorials/oauth/java/managedidentity/producer

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.sql.Timestamp;

public class TestDataReporter implements Runnable {

    private static final int NUM_MESSAGES = 100;
    private final String TOPIC;

    private Producer<Long, String> producer;

    public TestDataReporter(final Producer<Long, String> producer, String TOPIC) {
        this.producer = producer;
        this.TOPIC = TOPIC;
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            for(int i = 0; i < NUM_MESSAGES; i++) {                
                try {
                    long time = System.currentTimeMillis();
                    System.out.println("*** DEBUG ***  Test Data #" + i + " from thread #" + Thread.currentThread().getId());
                    final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(TOPIC, time, "Test Data #" + i);
                    producer.send(record, new Callback() {
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            if (exception != null) {
                                System.out.println(exception);
                                System.exit(1);
                            }
                        }
                    });
                } catch (Exception e) {
                    System.out.println("*** DEBUG ***  Exception sending message: " + e);
                    running = false;
                }
            }
            System.out.println("*** DEBUG ***  Finished sending " + NUM_MESSAGES + " messages from thread #" + Thread.currentThread().getId() + "!");
            try {
                Thread.sleep(60*1000*5);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}