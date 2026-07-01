package com.espigapedidos.espigapedidos.config;

import com.espigapedidos.espigapedidos.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String LOGIN_URL = "/login";
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(usuarioService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                    .authenticationProvider(authenticationProvider())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/css/**", "/js/**", "/uploads/**").permitAll()
                            .requestMatchers(LOGIN_URL, "/setup-admin", "/setup-tienda", "/reset/**").permitAll()
                            .requestMatchers("/usuarios/**", "/productos/**", "/tiendas/**").hasRole("ADMIN")
                            .requestMatchers("/pedidos/**", "/pedidos-especiales/**").hasAnyRole("ADMIN", "TIENDA")
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage(LOGIN_URL)
                            .loginProcessingUrl(LOGIN_URL)
                            .defaultSuccessUrl("/", true)
                            .failureUrl(LOGIN_URL + "?error")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl(LOGIN_URL + "?logout")
                            .permitAll()
                    );

            return http.build();
        } catch (Exception e) {
            throw new SecurityConfigException("Error configurando Spring Security", e);
        }
    }
}