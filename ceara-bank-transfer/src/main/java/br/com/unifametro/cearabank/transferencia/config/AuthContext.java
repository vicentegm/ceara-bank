package br.com.unifametro.cearabank.transferencia.config;

/**
 * Utilitário para armazenar o Token JWT na Thread atual (ThreadLocal).
 * Isso permite que o token seja extraído do filtro e repassado para o RestTemplate
 * sem que o token precise ser passado explicitamente entre métodos.
 */
public class AuthContext {

    // ThreadLocal é necessário para garantir que o token seja isolado por requisição.
    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    /**
     * Armazena o token Bearer JWT na Thread atual.
     * @param token O token Bearer (ex: "Bearer <token_jwt>").
     */
    public static void setToken(String token) {
        currentToken.set(token);
    }

    /**
     * Recupera o token Bearer JWT da Thread atual.
     * @return O token Bearer ou null se não houver token.
     */
    public static String getToken() {
        return currentToken.get();
    }

    /**
     * Limpa o token da Thread atual após o término da requisição.
     * CRUCIAL para evitar vazamento de tokens entre requisições reusadas pelo pool de threads.
     */
    public static void clear() {
        currentToken.remove();
    }
}
