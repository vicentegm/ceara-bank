package br.com.unifametro.cearabank.seguranca.model;

import jakarta.persistence.*;
import lombok.Getter; // Adicionando Getter e Setter para controle
import lombok.Setter; // Adicionando Getter e Setter para controle
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter 
@Setter 
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Este campo
    
    @Column(nullable = false)
    private String password; // E este campo

    // Construtor para registro
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Implementação UserDetails (Sobrescrita explícita dos métodos) ---
    
    @Override
    public String getPassword() {
        return password; // Implementação Manual
    }

    @Override
    public String getUsername() {
        return username; // Implementação Manual
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList((GrantedAuthority) () -> "ROLE_USER");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}