package com.example.kafkacloudstream.domain.catalog;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomCatalog implements Serializable {
    private Map<String, Map<String, Catalog>> catalogs;
    private FileProperties file;
    private java.lang.Error error;
}


