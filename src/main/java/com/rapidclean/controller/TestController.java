package com.rapidclean.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Test controller fonctionne !";
    }
    
    @GetMapping("/admin/test")
    @ResponseBody
    public String adminTest() {
        return "Admin test controller fonctionne !";
    }
}
