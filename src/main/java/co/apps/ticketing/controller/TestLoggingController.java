package co.apps.ticketing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestLoggingController {
    @PostMapping("/test")
    public String testEndpoint(@RequestBody String data) {
        return "Received: " + data;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
