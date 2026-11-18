package br.pucminas.moedaestudantil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Libera o front-end React (outra origem) para consumir a API REST.
 * Na nuvem, CORS_ORIGINS recebe o endereço da Vercel; padrões como https://*.vercel.app também são aceitos.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origensPermitidas;

    /** A origem enviada pelo navegador nunca termina com "/", por isso a barra copiada junto com a URL é retirada. */
    public CorsConfig(@Value("${app.cors.allowed-origins}") String[] origensPermitidas) {
        this.origensPermitidas = Arrays.stream(origensPermitidas)
                .map(origem -> origem.strip().replaceAll("/+$", ""))
                .filter(origem -> !origem.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
