package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.AlunoRequestDTO;
import br.pucminas.moedaestudantil.dto.AlunoResponseDTO;
import br.pucminas.moedaestudantil.dto.AlunoResumoDTO;
import br.pucminas.moedaestudantil.dto.ValidacaoCadastro;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.security.UsuarioAutenticado;
import br.pucminas.moedaestudantil.service.AlunoService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** O cadastro é público; consulta, alteração e exclusão valem apenas para o próprio aluno (US03). */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDTO> cadastrar(
            @RequestBody @Validated({Default.class, ValidacaoCadastro.class}) AlunoRequestDTO dto,
            UriComponentsBuilder uriBuilder) {
        Aluno aluno = alunoService.cadastrar(dto.toEntity(), dto.instituicaoId());
        URI uri = uriBuilder.path("/api/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
        return ResponseEntity.created(uri).body(AlunoResponseDTO.from(aluno));
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public AlunoResponseDTO buscar(@PathVariable Long id) {
        return AlunoResponseDTO.from(alunoService.buscarPorId(id));
    }

    /** Usado pelo professor para escolher o destinatário de um envio de moedas (RN06). */
    @GetMapping
    public List<AlunoResumoDTO> listar(@AuthenticationPrincipal UsuarioAutenticado professor) {
        return alunoService.listarDaInstituicaoDoProfessor(professor.id()).stream().map(AlunoResumoDTO::from).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public AlunoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid AlunoRequestDTO dto) {
        return AlunoResponseDTO.from(alunoService.atualizar(id, dto.toEntity(), dto.instituicaoId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        alunoService.excluir(id);
    }
}
