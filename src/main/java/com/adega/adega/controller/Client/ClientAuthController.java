package com.adega.adega.controller.Client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientAuthController {

    @GetMapping("/client/login")
    public String showClientLoginPage() {
        return "client/login";
    }
}
