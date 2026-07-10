package com.adega.adega.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {


    //pagina da tela de login adm
    @GetMapping("/login_adm")
    public String login() {
        return "admin/login_adm";
    }

    @GetMapping("/admin/home_adm")
    public String homeAdmin() {
        return "admin/home_adm";
    }


}
