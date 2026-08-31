package com.adega.adega.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

@RequestMapping("/admin")
public class AdminController {


    @GetMapping("/home")
    public String homeAdmin() {
        return "admin/home_adm";
    }
}
