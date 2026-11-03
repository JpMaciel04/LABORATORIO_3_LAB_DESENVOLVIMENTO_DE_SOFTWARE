package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.VantagemRequestDTO;
import br.pucminas.moedaestudantil.dto.VantagemResponseDTO;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.security.UsuarioAutenticado;
import br.pucminas.moedaestudantil.service.VantagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Catálogo exibido aos alunos (UC08) e manutenção feita pela empresa parceira (UC05).
 * As requisições de escrita são multipart: a parte "dados" traz o JSON e a parte "foto", o arquivo de imagem.
 */
@RestController
@RequestMapping("/api/vantagens")
public class VantagemController {

    private final VantagemService vantagemService;

    public VantagemController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VantagemResponseDTO> cadastrar(@RequestPart("dados") @Valid VantagemRequestDTO dto,
                                                         @RequestPart(value = "foto", required = false) MultipartFile foto,
                                                         @AuthenticationPrincipal UsuarioAutenticado empresa,
                                                         UriComponentsBuilder uriBuilder) {
        Vantagem vantagem = vantagemService.cadastrar(empresa.id(), dto.toEntity(), foto);
        URI uri = uriBuilder.path("/api/vantagens/{id}").buildAndExpand(vantagem.getId()).toUri();
        return ResponseEntity.created(uri).body(VantagemResponseDTO.from(vantagem));
    }

    @GetMapping
    public List<VantagemResponseDTO> listar() {
        return vantagemService.listarDisponiveis().stream().map(VantagemResponseDTO::from).toList();
    }

    @GetMapping("/minhas")
    public List<VantagemResponseDTO> listarMinhas(@AuthenticationPrincipal UsuarioAutenticado empresa) {
        return vantagemService.listarDaEmpresa(empresa.id()).stream().map(VantagemResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public VantagemResponseDTO buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado empresa) {
        return VantagemResponseDTO.from(vantagemService.buscarDaEmpresa(empresa.id(), id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VantagemResponseDTO atualizar(@PathVariable Long id, @RequestPart("dados") @Valid VantagemRequestDTO dto,
                                         @RequestPart(value = "foto", required = false) MultipartFile foto,
                                         @AuthenticationPrincipal UsuarioAutenticado empresa) {
        return VantagemResponseDTO.from(vantagemService.atualizar(empresa.id(), id, dto.toEntity(), foto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado empresa) {
        vantagemService.excluir(empresa.id(), id);
    }
}
