package com.wds.maytapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class MessageController {
    
    @GetMapping("/hello")
    public String sayHello() {  
        return "Hello, World!";
    }  
}
