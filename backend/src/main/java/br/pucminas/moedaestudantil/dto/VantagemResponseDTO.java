package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Vantagem;

public record VantagemResponseDTO(Long id, String nome, String descricao, String fotoUrl, int custoMoedas,
                                  boolean ativa, String empresa) {

    public static VantagemResponseDTO from(Vantagem vantagem) {
        return new VantagemResponseDTO(vantagem.getId(), vantagem.getNome(), vantagem.getDescricao(),
                vantagem.getFotoUrl(), vantagem.getCustoMoedas(), vantagem.isAtiva(), vantagem.getEmpresa().getNome());
    }
}
