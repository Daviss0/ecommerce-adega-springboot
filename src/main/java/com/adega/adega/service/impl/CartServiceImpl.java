package com.adega.adega.service.impl;


import com.adega.adega.dto.cart.AddCartDTO;
import com.adega.adega.dto.cart.CartDTO;
import com.adega.adega.entity.Cart;
import com.adega.adega.entity.CartItem;
import com.adega.adega.entity.Client;
import com.adega.adega.entity.Product;
import com.adega.adega.exception.CartException;
import com.adega.adega.exception.ProductNotFoundException;
import com.adega.adega.mapper.CartMapper;
import com.adega.adega.repository.CartItemRepository;
import com.adega.adega.repository.CartRepository;
import com.adega.adega.repository.ClientRepository;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public CartDTO getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public void addProduct(String email, AddCartDTO dto) {

        if(dto == null) {
            throw new IllegalArgumentException("Os dados do produto são obrigatórios.");
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        Cart cart = getOrCreateCart(email);
        Product product = getValidProduct(dto.getProductId());

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {
            validateStock(product, dto.getQuantity());

            CartItem newItem = new CartItem();

            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(dto.getQuantity());

            cart.getItems().add(newItem);
        }
        else {
            int newQuantity = cartItem.getQuantity() + dto.getQuantity();

            validateStock(product, newQuantity);
            cartItem.setQuantity(newQuantity);
        }

        touchCart(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void increaseQuantity(String email, Long cartItemId) {

        Cart cart = getCartByEmail(email);

        CartItem item = getClientCartItem(cart, cartItemId);

        Product product = item.getProduct();

        validateProductAvailability(product);

        int newQuantity = item.getQuantity() + 1;
        validateStock(product, newQuantity);

        item.setQuantity(newQuantity);

        touchCart(cart);

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void decreaseQuantity(String email, Long cartItemId) {

        Cart cart = getCartByEmail(email);

        CartItem item = getClientCartItem(cart, cartItemId);

        int newQuantity = item.getQuantity() - 1;

        if(newQuantity <= 0) {
            throw new IllegalArgumentException("A quantidade do produto não pode ser zero ou negativa.");
        }

        item.setQuantity(newQuantity);
        touchCart(cart);

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItem(String email, Long cartItemId) {

        Cart cart = getCartByEmail(email);

        CartItem item = getClientCartItem(cart, cartItemId);

        cart.getItems().remove(item);

        touchCart(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String email) {

        Cart cart = getCartByEmail(email);

        cart.getItems().clear();
        touchCart(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalItems(String email) {

        String normalizedEmail = normalizeEmail(email);

        Cart cart = cartRepository.findByClientUserEmail(normalizedEmail).orElse(null);

        if (cart == null) {
            return 0;
        }

        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(String email) {
        String normalizedEmail = normalizeEmail(email);

        Cart cart = cartRepository.findByClientUserEmail(normalizedEmail).orElse(null);

        if (cart == null) {
            return BigDecimal.ZERO;
        }
        return cart.getTotal();
    }

    //METODOS AUXILIARES

    private Cart getOrCreateCart(String email) {
        String normalizedEmail = normalizeEmail(email);

        return cartRepository.findByClientUserEmail(normalizedEmail).orElseGet(() ->{
            Client client = clientRepository.findByUser_Email(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

            Cart cart = new Cart();
            cart.setClient(client);
            return cartRepository.save(cart);
        });
    }

    private Cart getCartByEmail(String email) {

        String normalizedEmail = normalizeEmail(email);

        return cartRepository.findByClientUserEmail(normalizedEmail)
                .orElseThrow(() ->
                        new CartException("Carrinho não encontrado.")
                );
    }

    private CartItem getClientCartItem(Cart cart, Long cartItemId) {
        if (cartItemId == null) {
            throw new IllegalArgumentException("Item do carrinho não informado.");
        }
        return cart.getItems().stream().filter(item -> cartItemId.equals(item.getId()))
                .findFirst().orElseThrow(() -> new CartException("Item não encontrado no carrinho."));
    }

    private Product getValidProduct(Long productId) {
        if(productId == null) {
            throw new IllegalArgumentException("Produto não informado.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado."));

        validateProductAvailability(product);
        return product;
    }

    private void validateProductAvailability(Product product) {
        if(!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("Este produto não está disponível para venda.");
        }

        if(product.getStock() == null || product.getStock() <= 0) {
            throw new IllegalArgumentException("Produto sem estoque disponível.");
        }
    }

    private void validateStock(Product product, int desiredQuantity) {
        if (desiredQuantity <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        if (product.getStock() == null || product.getStock() < desiredQuantity) {
            throw new IllegalArgumentException("Estoque insuficiente para a quantidade desejada.");
        }
    }



    private String normalizeEmail(String email) {
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do cliente não informado.");
        }
        return email.trim().toLowerCase();
    }

    private void touchCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
    }
}
