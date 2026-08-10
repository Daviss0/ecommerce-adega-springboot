package com.adega.adega.controller.Client;

import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.dto.client.ClientRegistrationDTO;
import com.adega.adega.dto.client.ClientResponseDTO;
import com.adega.adega.dto.client.ClientUpdateDTO;
import com.adega.adega.service.AddressService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;
    private final AddressService addressService;

    public ClientController(ClientService clientService, AddressService addressService) {
        this.clientService = clientService;
        this.addressService = addressService;
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
    public String register(
            @Valid
            @ModelAttribute("clientRegistrationDTO")
            ClientRegistrationDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "client/register";
        }

        try {
            clientService.register(dto);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cadastro realizado com sucesso!"
            );

            return "redirect:/store/homepage";
        }
        catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "registerError",
                    exception.getMessage()
            );

            return "client/register";
        }
    }


    //minha conta
    @GetMapping("/account")
    public String account (Authentication authentication, Model model) {

        String email = authentication.getName();

        populateAccountModel(email, model);
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

    @PostMapping("/account/update")
    public String updateAccount (Authentication authentication,
                                 @Valid @ModelAttribute("clientUpdateDTO") ClientUpdateDTO dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        String currentEmail = authentication.getName();

        if(bindingResult.hasErrors()) {
            populateAccountModel(currentEmail, model);
            return "client/account";
        }
        try {
            clientService.update(currentEmail, dto);

            if(!currentEmail.equalsIgnoreCase(dto.getEmail())) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                redirectAttributes.addFlashAttribute("successMessage",
                        "E-mail alterado com sucesso. Faça o login novamente com o novo e-mail.");
                return "redirect:/client/login";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Dados atualizados com sucesso!");
            return "redirect:/client/account";
        }
        catch(IllegalArgumentException exception) {
            populateAccountModel(currentEmail, model);
            bindingResult.reject("updateError", exception.getMessage());
            return "client/account";
        }
        }

        public void populateAccountModel(String email, Model model) {
        ClientResponseDTO client = clientService.findByEmail(email);
        model.addAttribute("client", client);

        if (!model.containsAttribute("clientUpdateDTO")) {
            ClientUpdateDTO clientUpdateDTO = clientService.getUpdateData(email);

            model.addAttribute("clientUpdateDTO", clientUpdateDTO);
        }

        model.addAttribute("addresses", addressService.findAllByClientEmail(email));

        if (!model.containsAttribute("addressDTO")) {
            model.addAttribute("addressDTO", new AddressDTO());
        }

        if (!model.containsAttribute("editAddressDTO")) {
            model.addAttribute("editAddressDTO", new AddressDTO());
        }
        }

    }



