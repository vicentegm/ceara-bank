package br.com.unifametro.cearabank; 

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal do microserviço Ceará Bank Notificação.
 * A anotação ComponentScan é essencial para evitar o erro de Bean duplicado.
 */
@SpringBootApplication
@ComponentScan(basePackages = "br.com.unifametro.cearabank") // Garante que o Spring escaneie o pacote base corretamente
public class CearaBankNotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CearaBankNotificacaoApplication.class, args);
    }
}