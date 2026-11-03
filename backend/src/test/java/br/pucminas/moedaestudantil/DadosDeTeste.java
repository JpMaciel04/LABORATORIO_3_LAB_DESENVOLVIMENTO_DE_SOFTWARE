package br.pucminas.moedaestudantil;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Endereco;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.security.JwtService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Objetos usados por vários testes. */
public final class DadosDeTeste {

    public static final byte[] BYTES_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13};
    public static final byte[] BYTES_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16};

    private DadosDeTeste() {
    }

    public static Aluno aluno(String email, String cpf, InstituicaoEnsino instituicao) {
        Aluno aluno = new Aluno();
        aluno.setNome("Joana Silva");
        aluno.setEmail(email);
        aluno.setSenha("hash");
        aluno.setCpf(cpf);
        aluno.setRg("MG-12.345.678");
        aluno.setCurso("Engenharia de Software");
        aluno.setEndereco(new Endereco("Rua Dom José Gaspar", "500", null, "Coração Eucarístico",
                "Belo Horizonte", "MG", "30535901"));
        aluno.setInstituicao(instituicao);
        return aluno;
    }

    public static EmpresaParceira empresa(String email, String cnpj) {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome("Cantina Central");
        empresa.setEmail(email);
        empresa.setSenha("hash");
        empresa.setCnpj(cnpj);
        return empresa;
    }

    public static MockMultipartFile foto(byte[] conteudo) {
        return new MockMultipartFile("foto", "foto.png", "image/png", conteudo);
    }

    /** Autentica a requisição com um token real, que passa pelo mesmo filtro das requisições do front-end. */
    public static RequestPostProcessor autenticadoComo(Usuario usuario, JwtService jwtService) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + jwtService.gerarToken(usuario));
            return request;
        };
    }
}
