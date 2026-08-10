package com.adega.adega.controller.Client;

import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/client/account/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public String createAddress(@Valid @ModelAttribute("addressDTO")AddressDTO addressDTO,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressDTO", bindingResult);

            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);

            redirectAttributes.addFlashAttribute("openAddressModal", true);

            return "redirect:/client/account";
        }
        try {
            addressService.create(addressDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Endereço cadastrado com sucesso!");

            return "redirect:/client/account";
        }
        catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
            redirectAttributes.addFlashAttribute("openAddressModal", true);
            return "redirect:/client/account";
        }
    }

    @PostMapping("/{addressId}/edit")
    public String updateAddress(@PathVariable Long addressId,
                                @Valid @ModelAttribute("editAddressDTO") AddressDTO addressDTO,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            addressDTO.setId(addressId);

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editAddressDTO", bindingResult);
            redirectAttributes.addFlashAttribute("editAddressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("openEditAddressModal", true);

            return "redirect:/client/account";
        }

        try {

            addressService.update(addressId, addressDTO, authentication.getName());

            redirectAttributes.addFlashAttribute("successMessage", "Endereço atualizado com sucesso!");

            return "redirect:/client/account";
        }
        catch (IllegalArgumentException exception) {
            addressDTO.setId(addressId);
            redirectAttributes.addFlashAttribute("editAddressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("editAddressError", exception.getMessage());
            redirectAttributes.addFlashAttribute("openEditAddressModal", true);
            return "redirect:/client/account";
        }
    }

    @PostMapping("/{addressId}/delete")
    public String deleteAddress(@PathVariable Long addressId, Authentication authentication, RedirectAttributes redirectAttributes) {
        addressService.delete(addressId, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Endereço excluído com sucesso!");
        return "redirect:/client/account";
    }

    @PostMapping("/{addressId}/principal")
    public String setPrincipalAddress(@PathVariable Long addressId, Authentication authentication, RedirectAttributes redirectAttributes) {
        addressService.setAsPrincipal(addressId, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Endereço principal atualizado com sucesso!");
        return "redirect:/client/account";
    }
}
