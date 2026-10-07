package com.example.banking.services.utils;

import com.example.banking.services.enums.FormatDateEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GenericUtil.class})
public class GenericUtilTest {

    @Test
    void jsonObjectToString_error(){
        try{
            GenericUtil.jsonObjectToString("");
        }catch (RuntimeException e) {
            assertEquals(e.getMessage(), "Error converting the Java object to json");
        }
    }

    @Test
    void convertStringToDate_error(){
        try{
            GenericUtil.convertStringToDate("tetetet", FormatDateEnum.yyyy_MM_dd_T_HH_mm_ss_Z);
        }catch (RuntimeException e) {
            assertEquals(e.getMessage(), "Error convertStringToDate");
        }
    }

}
