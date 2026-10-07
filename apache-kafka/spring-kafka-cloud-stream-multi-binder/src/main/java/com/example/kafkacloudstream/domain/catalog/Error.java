package com.example.kafkacloudstream.domain.catalog;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Error implements Serializable {
    private Long tolerancePercentage;
}


