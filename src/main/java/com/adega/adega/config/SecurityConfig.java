package com.adega.adega.config;

import com.adega.adega.enumerated.Role;
import com.adega.adega.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class SecurityConfig {


    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
                                                        @Qualifier("adminAuthenticationProvider") DaoAuthenticationProvider adminAuthenticationProvider) throws Exception {
        http
                .securityMatcher(
                        "/admin/**",
                                 "/login_adm",
                                 "/admin/login")
                .authenticationProvider(adminAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        "/login_adm",
                        "/admin/login"
                ).permitAll()
                        .requestMatchers("/admin/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login_adm")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin/home_adm", true)
                        .failureUrl("/login_adm?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/login_adm?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                );


        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain clientSecurityFilterChain(HttpSecurity http,
                                                         @Qualifier("clientAuthenticationProvider") DaoAuthenticationProvider clientAuthenticationProvider) throws Exception {
        http
                .authenticationProvider(clientAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console())
                        .permitAll()

                        .requestMatchers(
                                "/",
                                "/store/**",
                                "/products/image/**",
                                "/client/login",
                                "/client/register",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        .requestMatchers("/client/**")
                        .hasRole("CLIENT")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/client/login")
                        .loginProcessingUrl("/client/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/store/homepage", true)
                        .failureUrl("/client/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/client/logout")
                        .logoutSuccessUrl("/store/homepage?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                )

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PathRequest.toH2Console()
                        )
                )

                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );
        return http.build();
    }

    //carrega somente ADMIN ou EMPLOYEE
    @Bean
    public UserDetailsService adminUserDetailsService( UserRepository userRepository) {
        return email -> {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário administrativo não encontrado"));

            if (user.getRole() != Role.ADMIN && user.getRole() != Role.EMPLOYEE) {
                throw new UsernameNotFoundException("Usuário administrativo não encontrado");
            }
            return createUserDetails(user);
        };
    }

    //carrega somente CLIENT
    @Bean
    public UserDetailsService clientUserDetailsService(UserRepository userRepository) {
        return email -> {
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

            if(user.getRole() != Role.CLIENT) {
                throw new UsernameNotFoundException("Cliente não encontrado");
            }
            return createUserDetails(user);
        };
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider(@Qualifier("adminUserDetailsService") UserDetailsService adminUserDetailsService,
                                                                 PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider clientAuthenticationProvider(@Qualifier("clientUserDetailsService") UserDetailsService clientUserDetailsService,
                                                                  PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(clientUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    private UserDetails createUserDetails(com.adega.adega.entity.User user) {
        if(!Boolean.TRUE.equals(user.getActive())) {
            throw new DisabledException("Usuário inativo");
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }
}
