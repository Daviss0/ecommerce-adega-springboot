package com.adega.adega.service.impl;


import com.adega.adega.entity.Cart;
import com.adega.adega.entity.CartItem;
import com.adega.adega.entity.Client;
import com.adega.adega.entity.Product;
import com.adega.adega.entity.User;
import com.adega.adega.enumerated.Role;
import com.adega.adega.repository.CartRepository;
import com.adega.adega.repository.ClientRepository;
import com.adega.adega.repository.ProductRepository;
import com.adega.adega.repository.UserRepository;
import com.adega.adega.service.AbandonedCartService;
import com.adega.adega.service.CartService;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AbandonedCartServiceImplTest {

    @Autowired
    private AbandonedCartService abandonedCartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CartService cartService;

    @Test
    void shouldClearItemsFromAbandonedCartWithoutDeleting() {

        User user = new User();

        user.setName("Client Teste");
        user.setEmail("teste-abandoned-cart@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();

        client.setUser(user);
        client.setPhone("11999999999");

        client = clientRepository.save(client);

        Product product = new Product();

        product.setName("Produto Teste");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();


        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId
        );

        entityManager.clear();

        int cleanedCarts = abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(1, cleanedCarts);

        assertTrue(cartRepository.existsById(cartId),
                "O carrinho não deve ser excluído");

        Cart cartAfterCleanup = cartRepository.findById(cartId).orElseThrow();

        assertTrue(cartAfterCleanup.getItems().isEmpty(),
                "Os itens do carrinho abandonado devem ser removidos");
    }

    @Test
    void shouldNotClearItemsFromRecentCart() {

        User user = new User();
        user.setName("Cliente Recente");
        user.setEmail("teste-recent-cart@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11988888888");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Recente");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("15.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        entityManager.clear();

        int cleanedCarts = abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(0, cleanedCarts, "Carrinho recente não deve ser considerado abandonado");

        assertTrue(cartRepository.existsById(cartId), "O carrinho deve continuar existindo");

        Cart cartAfterCleanup = cartRepository.findById(cartId).orElseThrow();

        assertFalse(cartAfterCleanup.getItems().isEmpty(), "Os itens de um carrinho recente não devem ser removidos");

        assertEquals(1, cartAfterCleanup.getItems().size(),
                "O carrinho deve continuar com seu item");

    }

    @Test
    void shouldIgnoreOldEmptyCart() {

        User user = new User();
        user.setName("Cliente Carrinho vazio");
        user.setEmail("teste-empty-old-cart@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11977777777");

        client = clientRepository.save(client);

        Cart cart = new Cart();
        cart.setClient(client);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId);

        entityManager.clear();

        int cleanedCarts = abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(0, cleanedCarts, "Carrinho vazio não deve ser considerado abandonado");

        assertTrue(
                cartRepository.existsById(cartId),
                "O carrinho vazio deve continuar existindo"
        );

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterCleanup.getItems().isEmpty(),
                "O carrinho deve continuar vazio"
        );
    }

    @Test
    void shouldNotClearCartBeforeThirtyDays() {

        User user = new User();
        user.setName("Cliente Limite");
        user.setEmail("teste-limit-cart@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11966666666");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Limite");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("20.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(
                        LocalDateTime.now()
                                .minusDays(29)
                                .minusHours(23)
                ),
                cartId
        );

        entityManager.clear();

        int cleanedCarts =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(
                0,
                cleanedCarts,
                "Carrinho com menos de 30 dias não deve ser considerado abandonado"
        );

        assertTrue(
                cartRepository.existsById(cartId),
                "O carrinho deve continuar existindo"
        );

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertEquals(
                1,
                cartAfterCleanup.getItems().size(),
                "Os itens não devem ser removidos antes de completar 30 dias"
        );
    }

    @Test
    void shouldClearAllItemsFromAbandonedCartWithMultipleProducts() {

        User user = new User();
        user.setName("Cliente Varios Produtos");
        user.setEmail("teste-multiple-items@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11955555555");

        client = clientRepository.save(client);

        Product product1 = new Product();
        product1.setName("Produto Teste 1");
        product1.setCategory("Teste");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStock(10);
        product1.setActive(true);

        product1 = productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Produto Teste 2");
        product2.setCategory("Teste");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setStock(10);
        product2.setActive(true);

        product2 = productRepository.save(product2);

        Product product3 = new Product();
        product3.setName("Produto Teste 3");
        product3.setCategory("Teste");
        product3.setPrice(new BigDecimal("30.00"));
        product3.setStock(10);
        product3.setActive(true);

        product3 = productRepository.save(product3);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item1 = new CartItem();
        item1.setProduct(product1);
        item1.setQuantity(1);

        CartItem item2 = new CartItem();
        item2.setProduct(product2);
        item2.setQuantity(2);

        CartItem item3 = new CartItem();
        item3.setProduct(product3);
        item3.setQuantity(3);

        cart.addItem(item1);
        cart.addItem(item2);
        cart.addItem(item3);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId
        );

        entityManager.clear();

        int cleanedCarts =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(
                1,
                cleanedCarts,
                "Deve contabilizar um carrinho limpo"
        );

        assertTrue(
                cartRepository.existsById(cartId),
                "O carrinho não deve ser excluído"
        );

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterCleanup.getItems().isEmpty(),
                "Todos os itens do carrinho abandonado devem ser removidos"
        );
    }

    @Test
    void shouldClearMultipleAbandonedCarts() {

        // Arrange - produto compartilhado
        Product product = new Product();
        product.setName("Produto Multiplos Carrinhos");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(20);
        product.setActive(true);

        product = productRepository.save(product);


        User user1 = new User();
        user1.setName("Cliente Abandonado 1");
        user1.setEmail("abandoned-cart-1@teste.com");
        user1.setPassword("senha123");
        user1.setRole(Role.CLIENT);
        user1.setActive(true);

        user1 = userRepository.save(user1);

        Client client1 = new Client();
        client1.setUser(user1);
        client1.setPhone("11944444444");

        client1 = clientRepository.save(client1);

        Cart cart1 = new Cart();
        cart1.setClient(client1);

        CartItem item1 = new CartItem();
        item1.setProduct(product);
        item1.setQuantity(1);

        cart1.addItem(item1);

        cart1 = cartRepository.saveAndFlush(cart1);

        Long cart1Id = cart1.getId();

        User user2 = new User();
        user2.setName("Cliente Abandonado 2");
        user2.setEmail("abandoned-cart-2@teste.com");
        user2.setPassword("senha123");
        user2.setRole(Role.CLIENT);
        user2.setActive(true);

        user2 = userRepository.save(user2);

        Client client2 = new Client();
        client2.setUser(user2);
        client2.setPhone("11933333333");

        client2 = clientRepository.save(client2);

        Cart cart2 = new Cart();
        cart2.setClient(client2);

        CartItem item2 = new CartItem();
        item2.setProduct(product);
        item2.setQuantity(2);

        cart2.addItem(item2);

        cart2 = cartRepository.saveAndFlush(cart2);

        Long cart2Id = cart2.getId();

        Timestamp abandonedDate =
                Timestamp.valueOf(LocalDateTime.now().minusDays(31));

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                abandonedDate,
                cart1Id
        );

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                abandonedDate,
                cart2Id
        );

        entityManager.clear();

        int cleanedCarts =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(
                2,
                cleanedCarts,
                "Os dois carrinhos abandonados devem ser processados"
        );

        Cart cart1AfterCleanup =
                cartRepository.findById(cart1Id).orElseThrow();

        Cart cart2AfterCleanup =
                cartRepository.findById(cart2Id).orElseThrow();

        assertTrue(
                cart1AfterCleanup.getItems().isEmpty(),
                "O primeiro carrinho deve ficar vazio"
        );

        assertTrue(
                cart2AfterCleanup.getItems().isEmpty(),
                "O segundo carrinho deve ficar vazio"
        );

        assertTrue(
                cartRepository.existsById(cart1Id),
                "O primeiro carrinho deve continuar existindo"
        );

        assertTrue(
                cartRepository.existsById(cart2Id),
                "O segundo carrinho deve continuar existindo"
        );
    }

    @Test
    void shouldNotProcessSameAbandonedCartTwice() {

        User user = new User();
        user.setName("Cliente Idempotencia");
        user.setEmail("teste-idempotencia@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11922222222");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Idempotencia");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("30.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId
        );

        entityManager.clear();

        int firstCleanup =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        entityManager.clear();

        int secondCleanup =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        assertEquals(
                1,
                firstCleanup,
                "A primeira execução deve limpar o carrinho abandonado"
        );

        assertEquals(
                0,
                secondCleanup,
                "A segunda execução não deve processar novamente o carrinho já vazio"
        );

        assertTrue(
                cartRepository.existsById(cartId),
                "O carrinho deve continuar existindo"
        );

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterCleanup.getItems().isEmpty(),
                "O carrinho deve permanecer vazio após a segunda execução"
        );
    }

    @Test
    void shouldPreserveCartAndClientWhenCleaningAbandonedCart() {

        User user = new User();
        user.setName("Cliente Preservado");
        user.setEmail("teste-preserve-client@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Long userId = user.getId();

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11911111111");

        client = clientRepository.save(client);

        Long clientId = client.getId();

        Product product = new Product();
        product.setName("Produto Preservacao");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("40.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId
        );

        entityManager.clear();

        int cleanedCarts =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();

        entityManager.clear();

        assertEquals(
                1,
                cleanedCarts,
                "O carrinho abandonado deve ser processado"
        );

        assertTrue(
                cartRepository.existsById(cartId),
                "O Cart deve continuar existindo"
        );

        assertTrue(
                clientRepository.existsById(clientId),
                "O Client não pode ser excluído"
        );

        assertTrue(
                userRepository.existsById(userId),
                "O User não pode ser excluído"
        );

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterCleanup.getItems().isEmpty(),
                "Somente os CartItems devem ser removidos"
        );

        assertEquals(
                clientId,
                cartAfterCleanup.getClient().getId(),
                "O Cart deve continuar associado ao mesmo Client"
        );
    }

    @Test
    void shouldNotUpdateCartActivityWhenOnlyViewingCart() {

        // Arrange - usuário
        User user = new User();
        user.setName("Cliente Visualizacao");
        user.setEmail("teste-visualizacao@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11988888888");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Visualizacao");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("20.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        LocalDateTime oldDate =
                LocalDateTime.now().minusDays(31);

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(oldDate),
                cartId
        );

        entityManager.clear();

        cartService.getCart("teste-visualizacao@teste.com");

        entityManager.clear();

        Cart cartAfterViewing =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterViewing.getUpdatedAt().isBefore(
                        LocalDateTime.now().minusDays(30)
                ),
                "Apenas visualizar o carrinho não deve renovar sua atividade"
        );
    }

    @Test
    void shouldUpdateCartActivityWhenProductQuantityIsIncreased() {

        // Arrange - usuário
        User user = new User();
        user.setName("Cliente Atividade");
        user.setEmail("teste-atividade@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11977777777");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Atividade");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();
        Long cartItemId = cart.getItems().get(0).getId();

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(31)),
                cartId
        );

        entityManager.clear();

        cartService.increaseQuantity(
                "teste-atividade@teste.com",
                cartItemId
        );

        cartRepository.flush();
        entityManager.clear();

        Cart cartAfterModification =
                cartRepository.findById(cartId).orElseThrow();

        assertTrue(
                cartAfterModification.getUpdatedAt()
                        .isAfter(LocalDateTime.now().minusMinutes(1)),
                "Modificar o carrinho deve renovar sua atividade"
        );

        assertEquals(
                2,
                cartAfterModification.getItems().get(0).getQuantity(),
                "A quantidade do produto deve ter sido aumentada"
        );
    }

    @Test
    void shouldNotUpdateCartActivityWhenCleaningAbandonedCart() {

        User user = new User();
        user.setName("Cliente Limpeza");
        user.setEmail("teste-limpeza-data@teste.com");
        user.setPassword("senha123");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11966666666");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Produto Limpeza");
        product.setCategory("Teste");
        product.setPrice(new BigDecimal("30.00"));
        product.setStock(10);
        product.setActive(true);

        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setClient(client);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        cart.addItem(item);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        LocalDateTime oldDate =
                LocalDateTime.now().minusDays(31);

        jdbcTemplate.update(
                "UPDATE carts SET updated_at = ? WHERE id = ?",
                Timestamp.valueOf(oldDate),
                cartId
        );

        entityManager.clear();

        int cleanedCarts =
                abandonedCartService.clearAbandonedCarts();

        cartRepository.flush();
        entityManager.clear();

        Cart cartAfterCleanup =
                cartRepository.findById(cartId).orElseThrow();

        assertEquals(
                1,
                cleanedCarts,
                "O carrinho abandonado deve ser processado"
        );

        assertTrue(
                cartAfterCleanup.getItems().isEmpty(),
                "Os itens do carrinho devem ser removidos"
        );

        assertTrue(
                cartAfterCleanup.getUpdatedAt()
                        .isBefore(LocalDateTime.now().minusDays(30)),
                "A limpeza automática não deve renovar a atividade do carrinho"
        );
    }
}
