package ec.com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        //producer.send(new ProducerRecord<String,String>("test10","KeyA","JavaApi"));
        long count = 0L;
        while (count < 200) {
            producer.send(new ProducerRecord<String, String>("test10", "KeyA", String.valueOf(count)));
            System.out.println("Send!..");
            count++;
        }

        producer.flush();
        producer.close();


    }
}
