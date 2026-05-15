package com.adega.adega.controller;


import com.adega.adega.entity.Users;
import com.adega.adega.enumerated.Role;
import com.adega.adega.service.UserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "keyword",required = false) String keyword,
                            Model model) {

        List<Users> users = userService.searchInternalUsers(keyword);

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);

        return "list_users";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new Users());
        model.addAttribute("roles", List.of(Role.ADMIN, Role.EMPLOYEE));

        return "form_user";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute Users users,
                           RedirectAttributes redirectAttributes) {

        userService.save(users);

        redirectAttributes.addFlashAttribute("successMessage", "Usuário salvo com sucesso!");
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Users user = userService.findById(id)
                .orElse(null);

        if(user == null || user.getRole() != Role.CLIENT) {
            redirectAttributes.addFlashAttribute("errorMessage","Usuário não encontrado");
            return "redirect:/admin/users";
        }

        user.setPassword("");

        model.addAttribute("user", user);
        model.addAttribute("roles", List.of(Role.ADMIN, Role.EMPLOYEE));

        return  "form_user";
    }

    @PostMapping("/activate/{id}")
    public String activateUser(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        userService.activateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuário ativado com sucesso!");

        return "redirect:/admin/users";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("successMessage","Usuário desativado com sucesso!");

        return "redirect:/admin/users";
    }
}
