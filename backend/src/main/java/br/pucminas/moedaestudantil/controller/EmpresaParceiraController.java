package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.EmpresaParceiraRequestDTO;
import br.pucminas.moedaestudantil.dto.EmpresaParceiraResponseDTO;
import br.pucminas.moedaestudantil.dto.ValidacaoCadastro;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.service.EmpresaParceiraService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

/** O cadastro é público; a listagem exige login; consulta, alteração e exclusão valem apenas para a própria empresa (US12). */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private final EmpresaParceiraService empresaParceiraService;

    public EmpresaParceiraController(EmpresaParceiraService empresaParceiraService) {
        this.empresaParceiraService = empresaParceiraService;
    }

    @PostMapping
    public ResponseEntity<EmpresaParceiraResponseDTO> cadastrar(
            @RequestBody @Validated({Default.class, ValidacaoCadastro.class}) EmpresaParceiraRequestDTO dto,
            UriComponentsBuilder uriBuilder) {
        EmpresaParceira empresa = empresaParceiraService.cadastrar(dto.toEntity());
        URI uri = uriBuilder.path("/api/empresas/{id}").buildAndExpand(empresa.getId()).toUri();
        return ResponseEntity.created(uri).body(EmpresaParceiraResponseDTO.from(empresa));
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public EmpresaParceiraResponseDTO buscar(@PathVariable Long id) {
        return EmpresaParceiraResponseDTO.from(empresaParceiraService.buscarPorId(id));
    }

    @GetMapping
    public List<EmpresaParceiraResponseDTO> listar() {
        return empresaParceiraService.listar().stream().map(EmpresaParceiraResponseDTO::from).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public EmpresaParceiraResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid EmpresaParceiraRequestDTO dto) {
        return EmpresaParceiraResponseDTO.from(empresaParceiraService.atualizar(id, dto.toEntity()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        empresaParceiraService.excluir(id);
    }
}
