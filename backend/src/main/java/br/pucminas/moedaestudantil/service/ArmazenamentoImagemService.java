package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

/** Guarda as fotos das vantagens em disco; a entidade Vantagem guarda apenas a URL (RN09). */
@Service
public class ArmazenamentoImagemService {

    public static final String URL_BASE = "/api/fotos/";

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoImagemService.class);
    private static final byte[] ASSINATURA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ASSINATURA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final Path diretorio;

    public ArmazenamentoImagemService(@Value("${app.upload.dir}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    /** @return a URL pública da foto gravada */
    public String salvar(MultipartFile foto) {
        String arquivo = UUID.randomUUID() + identificarExtensao(foto);
        try {
            Files.createDirectories(diretorio);
            foto.transferTo(diretorio.resolve(arquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar a foto da vantagem.", e);
        }
        return URL_BASE + arquivo;
    }

    /** Um arquivo que sobra não afeta o sistema, por isso a falha na remoção é apenas registrada. */
    public void remover(String fotoUrl) {
        Path arquivo = diretorio.resolve(Path.of(fotoUrl).getFileName());
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            log.warn("Não foi possível remover a foto {}", arquivo, e);
        }
    }

    /** Confere o conteúdo do arquivo, e não o nome ou o Content-Type, que são informados por quem envia (US13). */
    private static String identificarExtensao(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new RegraNegocioException("foto", "Envie a foto da vantagem em JPG ou PNG.");
        }
        try (InputStream conteudo = foto.getInputStream()) {
            byte[] cabecalho = conteudo.readNBytes(ASSINATURA_PNG.length);
            if (comecaCom(cabecalho, ASSINATURA_JPG)) {
                return ".jpg";
            }
            if (comecaCom(cabecalho, ASSINATURA_PNG)) {
                return ".png";
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto da vantagem.", e);
        }
        throw new RegraNegocioException("foto", "A foto deve estar em formato JPG ou PNG.");
    }

    private static boolean comecaCom(byte[] conteudo, byte[] assinatura) {
        return conteudo.length >= assinatura.length
                && Arrays.equals(Arrays.copyOf(conteudo, assinatura.length), assinatura);
    }
}
