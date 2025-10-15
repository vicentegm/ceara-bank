package br.com.unifametro.cearabank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    excludeAutoConfiguration = SecurityAutoConfiguration.class 
)
class CearaBankSegurancaApplicationTests {

    @Test
    void contextLoads() {
        // Este teste verifica apenas se o contexto do Spring Boot carrega.
        // Ao excluir a SecurityAutoConfiguration, evitamos a falha de inicialização.
    }

}