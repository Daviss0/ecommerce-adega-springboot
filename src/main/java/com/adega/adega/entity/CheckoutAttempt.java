package com.adega.adega.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "checkout_attempts",
        uniqueConstraints = { @UniqueConstraint(name = "uk_checkout_attempt_token", columnNames = "token")})
public class CheckoutAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private boolean used = false;

    //getters && setters
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

    public Client getClient() {return client;}

    public void setClient(Client client) {this.client = client;}

    public boolean isUsed() {return used;}

    public void setUsed(boolean used) {this.used = used;}
}
