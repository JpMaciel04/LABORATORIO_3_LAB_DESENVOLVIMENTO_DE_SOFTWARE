package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Foto;
import br.pucminas.moedaestudantil.repository.FotoRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda as fotos das vantagens no banco (tabela foto); a entidade Vantagem guarda apenas a URL (RN09).
 * Na nuvem, o disco do servidor é apagado a cada reinício, por isso as fotos não ficam mais em disco (Lab04S02).
 * As operações participam da transação de quem chama: se o cadastro da vantagem falhar, a foto também é descartada.
 */
@Service
public class ArmazenamentoImagemService {

    public static final String URL_BASE = "/api/fotos/";

    /** Fotos que acompanham o perfil demo (db/demo); ficam dentro do JAR, não no banco. */
    private static final String PASTA_DEMO = "fotos-demo/";
    /** Nomes gerados pelo sistema e das fotos de demonstração; impede caminhos como "../". */
    private static final Pattern NOME_VALIDO = Pattern.compile("[A-Za-z0-9-]+\\.(jpg|png)");
    private static final byte[] ASSINATURA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ASSINATURA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final FotoRepository fotoRepository;

    public ArmazenamentoImagemService(FotoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

    /** @return a URL pública da foto gravada */
    @Transactional
    public String salvar(MultipartFile foto) {
        String arquivo = UUID.randomUUID() + identificarExtensao(foto);
        try {
            fotoRepository.save(new Foto(arquivo, foto.getBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto da vantagem.", e);
        }
        return URL_BASE + arquivo;
    }

    /** As fotos de demonstração não estão no banco; para elas, a remoção não tem efeito. */
    @Transactional
    public void remover(String fotoUrl) {
        fotoRepository.removerPorArquivo(fotoUrl.substring(fotoUrl.lastIndexOf('/') + 1));
    }

    /** Procura a foto no banco e, se não estiver lá, entre as fotos de demonstração. */
    @Transactional(readOnly = true)
    public Optional<byte[]> buscar(String arquivo) {
        if (!NOME_VALIDO.matcher(arquivo).matches()) {
            return Optional.empty();
        }
        Optional<byte[]> doBanco = fotoRepository.findById(arquivo).map(Foto::getConteudo);
        return doBanco.isPresent() ? doBanco : buscarFotoDemo(arquivo);
    }

    private static Optional<byte[]> buscarFotoDemo(String arquivo) {
        ClassPathResource recurso = new ClassPathResource(PASTA_DEMO + arquivo);
        if (!recurso.exists()) {
            return Optional.empty();
        }
        try (InputStream conteudo = recurso.getInputStream()) {
            return Optional.of(conteudo.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto " + arquivo, e);
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
