package com.adega.adega.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {


    @GetMapping("/login_adm")
    public String login() {
        return "admin/login_adm";
    }

}
