package br.com.unifametro.cearabank.contas.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiagnosticoController {

    /**
     * Retorna o hostname da instância atual para verificar o load balancer.
     * #6073 - Endpoint criado para facilitar o teste de escalabilidade horizontal.
     */
    @GetMapping("/status")
    public String getStatus() {
        try {
            return "Respondendo da instancia: " + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Erro ao identificar hostname, maxo!";
        }
    }
}