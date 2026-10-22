package com.adega.adega.repository;


import com.adega.adega.entity.*;
import com.adega.adega.enumerated.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import com.adega.adega.enumerated.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;



    @Test
    void deveSomarQuantidadesDePedidosEntregues() {

        User user = new User();
        user.setName("Cliente Teste");
        user.setEmail("cliente-ranking@teste.com");
        user.setPassword("123456");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11999999999");

        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Cerveja Teste");
        product.setCategory("Cervejas");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(20);
        product.setActive(true);

        product = productRepository.save(product);

        for (int quantidade : new int[]{3, 5}) {

            Order order = new Order();
            order.setClient(client);
            order.setStatus(OrderStatus.DELIVERED);
            order.setTotalAmount(
                    product.getPrice().multiply(BigDecimal.valueOf(quantidade))
            );

            order.setDeliveryCep("01001000");
            order.setDeliveryStreet("Rua Teste");
            order.setDeliveryNumber("100");
            order.setDeliveryHood("Centro");
            order.setDeliveryCity("São Paulo");
            order.setDeliveryState("SP");

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(quantidade);
            item.setUnitPrice(product.getPrice());

            order.addItem(item);
            order = orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setPreviousStatus(OrderStatus.SHIPPED);
            history.setNewStatus(OrderStatus.DELIVERED);
            history.setChangedAt(LocalDateTime.now());
            history.setChangedBy("admin-teste");

            historyRepository.save(history);
        }

        historyRepository.flush();

        List<Object[]> resultado = orderItemRepository.findBestSellingProducts(
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(30)
        );

        assertEquals(1, resultado.size());

        Object[] linha = resultado.get(0);

        assertEquals(product.getId(), ((Number) linha[0]).longValue());
        assertEquals(8L, ((Number) linha[1]).longValue());
    }


    @Test
    void deveIgnorarPedidosNaoEntregues() {

        User user = new User();
        user.setName("Cliente Teste");
        user.setEmail("cliente-status@teste.com");
        user.setPassword("123456");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11999999999");
        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Cerveja Teste");
        product.setCategory("Cervejas");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(50);
        product.setActive(true);
        product = productRepository.save(product);

        OrderStatus[] status = {
                OrderStatus.DELIVERED,
                OrderStatus.PENDING,
                OrderStatus.CANCELED,
                OrderStatus.SHIPPED
        };

        int[] quantidades = {5, 10, 8, 6};

        for (int i = 0; i < status.length; i++) {

            Order order = new Order();
            order.setClient(client);
            order.setStatus(status[i]);
            order.setTotalAmount(
                    product.getPrice().multiply(BigDecimal.valueOf(quantidades[i]))
            );

            order.setDeliveryCep("01001000");
            order.setDeliveryStreet("Rua Teste");
            order.setDeliveryNumber("100");
            order.setDeliveryHood("Centro");
            order.setDeliveryCity("São Paulo");
            order.setDeliveryState("SP");

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(quantidades[i]);
            item.setUnitPrice(product.getPrice());

            order.addItem(item);
            order = orderRepository.saveAndFlush(order);

            if (status[i] == OrderStatus.DELIVERED) {

                OrderStatusHistory history = new OrderStatusHistory();
                history.setOrder(order);
                history.setPreviousStatus(OrderStatus.SHIPPED);
                history.setNewStatus(OrderStatus.DELIVERED);
                history.setChangedAt(LocalDateTime.now());
                history.setChangedBy("admin-teste");

                historyRepository.save(history);
            }
        }

        historyRepository.flush();

        List<Object[]> resultado = orderItemRepository.findBestSellingProducts(
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(30)
        );

        assertEquals(1, resultado.size());
        assertEquals(product.getId(), ((Number) resultado.get(0)[0]).longValue());
        assertEquals(5L, ((Number) resultado.get(0)[1]).longValue());
    }


    @Test
    void deveConsiderarSomenteEntregasDosUltimos30Dias() {

        User user = new User();
        user.setName("Cliente Teste");
        user.setEmail("cliente-periodo@teste.com");
        user.setPassword("123456");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11999999999");
        client = clientRepository.save(client);

        Product product = new Product();
        product.setName("Cerveja Teste");
        product.setCategory("Cervejas");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(30);
        product.setActive(true);
        product = productRepository.save(product);

        LocalDateTime agora = LocalDateTime.now();

        int[] quantidades = {4, 10, 3};
        int[] diasDesdeEntrega = {5, 45, 15};

        for (int i = 0; i < quantidades.length; i++) {

            Order order = new Order();
            order.setClient(client);
            order.setStatus(OrderStatus.DELIVERED);
            order.setTotalAmount(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(quantidades[i])
                    )
            );

            order.setDeliveryCep("01001000");
            order.setDeliveryStreet("Rua Teste");
            order.setDeliveryNumber("100");
            order.setDeliveryHood("Centro");
            order.setDeliveryCity("São Paulo");
            order.setDeliveryState("SP");

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(quantidades[i]);
            item.setUnitPrice(product.getPrice());

            order.addItem(item);
            order = orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setPreviousStatus(OrderStatus.SHIPPED);
            history.setNewStatus(OrderStatus.DELIVERED);
            history.setChangedAt(
                    agora.minusDays(diasDesdeEntrega[i])
            );
            history.setChangedBy("admin-teste");

            historyRepository.save(history);
        }

        historyRepository.flush();

        List<Object[]> resultado = orderItemRepository.findBestSellingProducts(
                OrderStatus.DELIVERED,
                agora.minusDays(30)
        );

        assertEquals(1, resultado.size());
        assertEquals(
                product.getId(),
                ((Number) resultado.get(0)[0]).longValue()
        );
        assertEquals(
                7L,
                ((Number) resultado.get(0)[1]).longValue()
        );
    }



    @Test
    void deveIgnorarProdutosInativosOuSemEstoque() {

        User user = new User();
        user.setName("Cliente Teste");
        user.setEmail("cliente-estoque@teste.com");
        user.setPassword("123456");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11999999999");
        client = clientRepository.save(client);

        String[] nomes = {"Cerveja A", "Whisky B", "Vinho C"};
        int[] estoques = {10, 20, 0};
        boolean[] ativos = {true, false, true};
        int[] quantidadesVendidas = {15, 30, 25};

        Product[] produtos = new Product[3];

        for (int i = 0; i < produtos.length; i++) {

            Product product = new Product();
            product.setName(nomes[i]);
            product.setCategory("Bebidas");
            product.setPrice(new BigDecimal("10.00"));
            product.setStock(estoques[i]);
            product.setActive(ativos[i]);

            produtos[i] = productRepository.save(product);
        }

        for (int i = 0; i < produtos.length; i++) {

            Order order = new Order();
            order.setClient(client);
            order.setStatus(OrderStatus.DELIVERED);
            order.setTotalAmount(
                    produtos[i].getPrice().multiply(
                            BigDecimal.valueOf(quantidadesVendidas[i])
                    )
            );

            order.setDeliveryCep("01001000");
            order.setDeliveryStreet("Rua Teste");
            order.setDeliveryNumber("100");
            order.setDeliveryHood("Centro");
            order.setDeliveryCity("São Paulo");
            order.setDeliveryState("SP");

            OrderItem item = new OrderItem();
            item.setProduct(produtos[i]);
            item.setProductName(produtos[i].getName());
            item.setQuantity(quantidadesVendidas[i]);
            item.setUnitPrice(produtos[i].getPrice());

            order.addItem(item);
            order = orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setPreviousStatus(OrderStatus.SHIPPED);
            history.setNewStatus(OrderStatus.DELIVERED);
            history.setChangedAt(LocalDateTime.now().minusDays(2));
            history.setChangedBy("admin-teste");

            historyRepository.save(history);
        }

        historyRepository.flush();

        List<Object[]> resultado = orderItemRepository.findBestSellingProducts(
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(30)
        );

        assertEquals(1, resultado.size());

        assertEquals(
                produtos[0].getId(),
                ((Number) resultado.get(0)[0]).longValue()
        );

        assertEquals(
                15L,
                ((Number) resultado.get(0)[1]).longValue()
        );
    }


    @Test
    void deveOrdenarProdutosPorQuantidadeVendidaDecrescente() {

        User user = new User();
        user.setName("Cliente Teste");
        user.setEmail("cliente-ranking-ordem@teste.com");
        user.setPassword("123456");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setPhone("11999999999");
        client = clientRepository.save(client);

        String[] nomes = {"Cerveja A", "Whisky B", "Vinho C"};
        int[] quantidadesVendidas = {5, 20, 12};

        Product[] produtos = new Product[3];

        for (int i = 0; i < produtos.length; i++) {

            Product product = new Product();
            product.setName(nomes[i]);
            product.setCategory("Bebidas");
            product.setPrice(new BigDecimal("10.00"));
            product.setStock(50);
            product.setActive(true);

            produtos[i] = productRepository.save(product);
        }

        for (int i = 0; i < produtos.length; i++) {

            Order order = new Order();
            order.setClient(client);
            order.setStatus(OrderStatus.DELIVERED);
            order.setTotalAmount(
                    produtos[i].getPrice().multiply(
                            BigDecimal.valueOf(quantidadesVendidas[i])
                    )
            );

            order.setDeliveryCep("01001000");
            order.setDeliveryStreet("Rua Teste");
            order.setDeliveryNumber("100");
            order.setDeliveryHood("Centro");
            order.setDeliveryCity("São Paulo");
            order.setDeliveryState("SP");

            OrderItem item = new OrderItem();
            item.setProduct(produtos[i]);
            item.setProductName(produtos[i].getName());
            item.setQuantity(quantidadesVendidas[i]);
            item.setUnitPrice(produtos[i].getPrice());

            order.addItem(item);
            order = orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setPreviousStatus(OrderStatus.SHIPPED);
            history.setNewStatus(OrderStatus.DELIVERED);
            history.setChangedAt(LocalDateTime.now().minusDays(2));
            history.setChangedBy("admin-teste");

            historyRepository.save(history);
        }

        historyRepository.flush();

        List<Object[]> resultado = orderItemRepository.findBestSellingProducts(
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(30)
        );

        assertEquals(3, resultado.size());

        assertEquals(
                produtos[1].getId(),
                ((Number) resultado.get(0)[0]).longValue()
        );
        assertEquals(20L, ((Number) resultado.get(0)[1]).longValue());

        assertEquals(
                produtos[2].getId(),
                ((Number) resultado.get(1)[0]).longValue()
        );
        assertEquals(12L, ((Number) resultado.get(1)[1]).longValue());

        assertEquals(
                produtos[0].getId(),
                ((Number) resultado.get(2)[0]).longValue()
        );
        assertEquals(5L, ((Number) resultado.get(2)[1]).longValue());
    }


}