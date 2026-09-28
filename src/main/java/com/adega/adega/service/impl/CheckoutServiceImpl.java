package com.adega.adega.service.impl;

import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.dto.cart.CartItemDTO;
import com.adega.adega.dto.checkout.CheckoutDTO;
import com.adega.adega.dto.checkout.CheckoutRequestDTO;
import com.adega.adega.dto.client.AddressDTO;
import com.adega.adega.entity.*;
import com.adega.adega.enumerated.OrderStatus;
import com.adega.adega.exception.*;
import com.adega.adega.mapper.CheckoutMapper;
import com.adega.adega.repository.*;
import com.adega.adega.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final AddressService addressService;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final CepValidationService cepValidationService;
    private final CheckoutMapper checkoutMapper;
    private final CheckoutAttemptRepository checkoutAttemptRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public CheckoutServiceImpl(CartService cartService, AddressService addressService, ClientRepository clientRepository,
                               ProductRepository productRepository, OrderRepository orderRepository, StockService stockService,
                               CepValidationService cepValidationService, CheckoutMapper checkoutMapper, CheckoutAttemptRepository checkoutAttemptRepository,
                               OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.stockService = stockService;
        this.cepValidationService = cepValidationService;
        this.checkoutMapper = checkoutMapper;
        this.checkoutAttemptRepository = checkoutAttemptRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public CheckoutDTO getCheckout(String email) {

        String normalizedEmail = normalizeEmail(email);

        CartDTO cart = cartService.getCartForCheckout(normalizedEmail);

        List<AddressDTO> addresses = addressService.findAllByClientEmail(normalizedEmail);
        if (addresses.isEmpty()) {
            throw new AddressNotFoundException("Cadastre um endereço de entrega antes de continuar.");
        }

        return checkoutMapper.toCheckoutDTO(cart, addresses);
    }

    @Override
    @Transactional
    public Order checkout(String email, CheckoutRequestDTO requestDTO) {

        String normalizedEmail = normalizeEmail(email);

        validateRequest(requestDTO);

        Client client =  clientRepository.findByUserEmail(normalizedEmail)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado"));

        int updated = checkoutAttemptRepository.consumeToken(requestDTO.getCheckoutToken(), client.getId());

        if (updated == 0) {
            throw new DuplicateCheckoutException("Este pedido já está sendo processado ou já foi confirmado.");
        }

        CartDTO cart = cartService.getCartForCheckout(normalizedEmail);

        AddressDTO address = addressService.findByIdAndClientEmail(requestDTO.getAddressId(), normalizedEmail);

        cepValidationService.validateDeliveryArea(address.getCep());

        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);

        fillAddressSnapshot(order, address);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDTO cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Um dos produtos do carrinho não está mais disponível."));

            validateProductForCheckout(product, cartItem.getQuantity());

            OrderItem orderItem = checkoutMapper.toOrderItem(cartItem, product);
            order.addItem(orderItem);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);
        }
            order.setTotalAmount(total);

            Order savedOrder = orderRepository.save(order);

            registerInitialStatusHistory(savedOrder, normalizedEmail);

            for (CartItemDTO cartItem : cart.getItems()) {
                stockService.removeStock(cartItem.getProductId(),
                        cartItem.getQuantity(),
                        "Venda - pedido #" + savedOrder.getId(),
                        normalizedEmail,
                        savedOrder);
            }

            cartService.clearCart(normalizedEmail);
            return savedOrder;

    }

    @Override
    @Transactional
    public String createCheckoutToken(String email) {

        String normalizedEmail = normalizeEmail(email);

        Client client = clientRepository.findByUserEmail(normalizedEmail)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado."));

        CheckoutAttempt attempt = new CheckoutAttempt();

        attempt.setToken(UUID.randomUUID().toString());
        attempt.setClient(client);
        attempt.setUsed(false);

        checkoutAttemptRepository.save(attempt);
        return attempt.getToken();
    }


    //metodos auxiliares
    private void validateRequest(CheckoutRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Os dados do checkout são obrigatórios.");
        }
        if (requestDTO.getAddressId() == null) {
            throw new IllegalArgumentException("Selecione um endereço de entrega.");
        }
        if (requestDTO.getCheckoutToken() == null || requestDTO.getCheckoutToken().isBlank()) {
            throw new IllegalArgumentException("Token de checkout inválido.");

        }
    }

    private void fillAddressSnapshot(Order order, AddressDTO address) {
        order.setDeliveryCep(address.getCep());
        order.setDeliveryStreet(address.getStreet());
        order.setDeliveryNumber(address.getNumber());
        order.setDeliveryComplement(address.getComplement());
        order.setDeliveryHood(address.getHood());
        order.setDeliveryCity(address.getCity());
        order.setDeliveryState("SP");
    }

    private void validateProductForCheckout(Product product, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade inválida no carrinho.");
        }

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductUnavailableException("O produto " + product.getName() + " Não está mais disponível para venda.");
        }

        if (product.getStock() == null || product.getStock() < quantity) {
            throw new InsufficientStockException("estoque insuficiente para o produto " + product.getName() + ".");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw  new IllegalArgumentException("Preço inválido para o produto " + product.getName() + ".");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do cliente não informado.");
        }
        return email.trim().toLowerCase();
    }

    private void registerInitialStatusHistory(Order order, String userName) {
        OrderStatusHistory history = new OrderStatusHistory();

        history.setOrder(order);
        history.setPreviousStatus(null);
        history.setNewStatus(OrderStatus.PENDING);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(userName);

        orderStatusHistoryRepository.save(history);
    }
}
