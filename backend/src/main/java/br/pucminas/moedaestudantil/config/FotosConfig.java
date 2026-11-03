package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.service.ArmazenamentoImagemService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** Publica as fotos das vantagens: as enviadas pelas empresas e as que acompanham o perfil demo. */
@Configuration
public class FotosConfig implements WebMvcConfigurer {

    private final String diretorio;

    public FotosConfig(@Value("${app.upload.dir}") String diretorio) {
        String uri = Path.of(diretorio).toAbsolutePath().normalize().toUri().toString();
        this.diretorio = uri.endsWith("/") ? uri : uri + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ArmazenamentoImagemService.URL_BASE + "**")
                .addResourceLocations(diretorio, "classpath:/fotos-demo/");
    }
}
