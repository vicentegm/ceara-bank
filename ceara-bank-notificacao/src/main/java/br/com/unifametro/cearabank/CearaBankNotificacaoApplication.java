package br.com.unifametro.cearabank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicação principal do microserviço CearaBank - Notificacao.
 * O pacote base (br.com.unifametro.cearabank) garante o Component Scan de 
 * todos os sub-pacotes (notificacao.controller, notificacao.service, etc.).
 */
@SpringBootApplication
@EnableAsync // Habilita a execução assíncrona para simular o processamento
public class CearaBankNotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CearaBankNotificacaoApplication.class, args);
    }
}
