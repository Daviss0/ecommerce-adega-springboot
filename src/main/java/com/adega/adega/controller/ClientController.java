package com.adega.adega.controller;


import com.adega.adega.entity.Client;
import com.adega.adega.service.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }


    @GetMapping
    public String listClients(@RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        List<Client> clients = clientService.search(keyword);

        model.addAttribute("clients", clients);
        model.addAttribute("keyword", keyword);

        return "list_clients";
    }

    @PostMapping("/delete/{id}")
    public String deactivateClient(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            clientService.deactivateClient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente excluido com sucesso!");
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente não encontrado.");
        }
        return "redirect:/admin/clients";
    }


}
