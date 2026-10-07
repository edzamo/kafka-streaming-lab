package ec.com.example.demoKafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

  @KafkaListener(
      topics = "test",
      groupId = "groupId"
  )
  void listener(String data) {
    System.out.println("Listener received :" + data + " :)");
  }
}
