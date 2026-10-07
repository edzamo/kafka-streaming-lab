package com.example.banking.services.api.util;

import com.example.banking.services.api.data.MockData;
import com.example.banking.services.server.models.Error;
import com.example.banking.services.utils.JsonSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;



@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JsonSerializer.class})
class JsonSerializerTest {

    @Autowired
    JsonSerializer jsonSerializer;


    @Test
    void testJsonStringToObjectEmpty() {
        Assertions.assertNull(jsonSerializer.jsonStringToObject("", Error.class));
    }

    @Test
    void testJsonStringToObjectSuccess() {
        Error error = jsonSerializer.jsonStringToObject(MockData.getErrorString(), Error.class);
        Assertions.assertEquals(MockData.getError(), error);
    }

    @Test
    void objectToStringSuccess() {
        Assertions.assertEquals(MockData.getErrorString(), jsonSerializer.jsonObjectToString(MockData.getError()));
    }

}
