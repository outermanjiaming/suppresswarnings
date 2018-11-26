package com.suppresswarnings.spring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

	@RequestMapping("/hello")
    public String index() {
        return "index.html";
    }
}
