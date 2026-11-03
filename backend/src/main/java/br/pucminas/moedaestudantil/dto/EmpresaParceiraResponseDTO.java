package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.EmpresaParceira;

/** Dados da empresa parceira devolvidos pela API. A senha nunca é exposta. */
public record EmpresaParceiraResponseDTO(Long id, String nome, String cnpj, String email) {

    public static EmpresaParceiraResponseDTO from(EmpresaParceira empresa) {
        return new EmpresaParceiraResponseDTO(empresa.getId(), empresa.getNome(), empresa.getCnpj(), empresa.getEmail());
    }
}
