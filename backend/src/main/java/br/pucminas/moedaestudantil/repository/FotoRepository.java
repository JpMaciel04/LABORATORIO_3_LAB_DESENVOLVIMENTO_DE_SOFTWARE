package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FotoRepository extends JpaRepository<Foto, String> {

    /** Remove sem carregar a imagem antes, como faria o deleteById. Não faz nada se o arquivo não existir. */
    @Modifying
    @Query("DELETE FROM Foto f WHERE f.arquivo = :arquivo")
    void removerPorArquivo(String arquivo);
}
