package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.service.ArmazenamentoImagemService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Publica as fotos das vantagens (RN09). A rota é pública porque a tag {@code <img>} não envia o token.
 * Cada foto nova recebe um nome novo, então o navegador pode guardá-la em cache por bastante tempo.
 */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final ArmazenamentoImagemService armazenamentoImagemService;

    public FotoController(ArmazenamentoImagemService armazenamentoImagemService) {
        this.armazenamentoImagemService = armazenamentoImagemService;
    }

    @GetMapping("/{arquivo}")
    public ResponseEntity<byte[]> buscar(@PathVariable String arquivo) {
        byte[] conteudo = armazenamentoImagemService.buscar(arquivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Foto " + arquivo + " não encontrada."));
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(arquivo).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(conteudo);
    }
}
