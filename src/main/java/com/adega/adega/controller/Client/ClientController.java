package com.adega.adega.controller.Client;

import com.adega.adega.dto.client.ClientRegistrationDTO;
import com.adega.adega.dto.client.ClientResponseDTO;
import com.adega.adega.dto.client.ClientUpdateDTO;
import com.adega.adega.exception.CepServiceUnavailableException;
import com.adega.adega.exception.InvalidCepException;
import com.adega.adega.service.CepValidationService;
import com.adega.adega.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService, CepValidationService cepValidationService) {
        this.clientService = clientService;
    }

    //cadastro
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if(!model.containsAttribute("clientRegistrationDTO")) {
            model.addAttribute("clientRegistrationDTO", new ClientRegistrationDTO());
        }
        return "client/register";
    }

    @PostMapping("/register")
    public String register (@Valid @ModelAttribute("clientRegistrationDTO") ClientRegistrationDTO dto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

       if(bindingResult.hasErrors()) {
           return "client/register";
       }

       try {
           clientService.register(dto);

           redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso!");
           return "redirect:/store/homepage";
       }
       catch (InvalidCepException exception) {
           bindingResult.rejectValue("billingAddress.cep", "invalid.cep", exception.getMessage());
           return "client/register";
       }
       catch (CepServiceUnavailableException exception) {
           bindingResult.rejectValue("billingAddress.cep", "cep.service.unavailable", "Não foi possível validar o CEP. Tente novamente." );
           return "client/register";
       }
       catch (IllegalArgumentException exception) {
           bindingResult.reject("registerError", exception.getMessage());
           return "client/register";
       }
    }


    //minha conta
    @GetMapping("/account")
    public String account (Authentication authentication, Model model) {

        String email = authentication.getName();

        ClientResponseDTO client = clientService.findByEmail(email);

        model.addAttribute("client", client);

        return "client/account";
    }

    //editar dados da conta
    @GetMapping("/account/edit")
    public String showEditForm(Authentication authentication, Model model) {

        String email = authentication.getName();

        ClientUpdateDTO dto = clientService.getUpdateData(email);

        model.addAttribute("clientUpdateDTO", dto);

        return "client/edit_account";
    }

    @PostMapping("/account/edit")
    public String updateAccount (Authentication authentication,
                                 @Valid @ModelAttribute("clientUpdateDTO") ClientUpdateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            return "client/edit_account";
        }

        String currentEmail = authentication.getName();

        try {
            clientService.update(currentEmail, dto);

            redirectAttributes.addFlashAttribute("successMessage", "Dados atualizados com sucesso!");

            if(!currentEmail.equalsIgnoreCase(dto.getEmail())) {
                return "redirect:/logout";
            }

            return "redirect:/client/account";
        }

        catch (IllegalArgumentException e) {
            bindingResult.reject("updateError", e.getMessage());

            return "client/edit_account";
        }
    }


}
