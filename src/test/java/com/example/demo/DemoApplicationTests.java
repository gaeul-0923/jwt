package com.example.demo;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

  @Test
  void contextLoads() {
  }


  @Test
  void jasypt() {
    String url = "jdbc:log4jdbc:mysql://localhost:3306/demo_test";
    String username = "root";
    String password = "0000";

    System.out.println("--------------------------------------------------");
    System.out.println(jasyptEncoding(url));
    System.out.println(jasyptEncoding(username));
    System.out.println(jasyptEncoding(password));
  }

  public String jasyptEncoding(String value) {

    String key = "hazleKey!@";
    StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
    pbeEnc.setAlgorithm("PBEWithMD5AndDES");
    pbeEnc.setPassword(key);
    return pbeEnc.encrypt(value);
  }

}
