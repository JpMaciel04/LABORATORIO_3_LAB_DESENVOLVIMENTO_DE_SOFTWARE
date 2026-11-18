package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Imagem de uma vantagem (RN09), gravada no banco para não depender do disco do servidor, que na nuvem
 * é apagado a cada reinício (Lab04S02). A vantagem guarda apenas a URL, que termina com o nome do arquivo.
 */
@Entity
@Table(name = "foto")
public class Foto {

    /** Mesmo limite de spring.servlet.multipart.max-file-size. */
    public static final int TAMANHO_MAXIMO = 5 * 1024 * 1024;

    /** Nome gerado pelo sistema: UUID + extensão (.jpg ou .png). */
    @Id
    @Column(length = 45)
    private String arquivo;

    @Column(nullable = false, length = TAMANHO_MAXIMO)
    private byte[] conteudo;

    protected Foto() {
    }

    public Foto(String arquivo, byte[] conteudo) {
        this.arquivo = arquivo;
        this.conteudo = conteudo;
    }

    public String getArquivo() {
        return arquivo;
    }

    public byte[] getConteudo() {
        return conteudo;
    }
}
