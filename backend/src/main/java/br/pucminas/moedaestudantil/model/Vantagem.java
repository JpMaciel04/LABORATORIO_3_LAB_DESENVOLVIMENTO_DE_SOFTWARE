package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Produto ou desconto oferecido por uma empresa parceira (RN09). */
@Entity
@Table(name = "vantagem")
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(name = "foto_url", nullable = false, length = 500)
    private String fotoUrl;

    @Column(name = "custo_moedas", nullable = false)
    private int custoMoedas;

    @Column(nullable = false)
    private boolean ativa = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaParceira empresa;

    protected Vantagem() {
    }

    public Vantagem(EmpresaParceira empresa, String nome, String descricao, String fotoUrl, int custoMoedas) {
        this.empresa = empresa;
        this.nome = nome;
        this.descricao = descricao;
        this.fotoUrl = fotoUrl;
        this.custoMoedas = custoMoedas;
    }

    /** A foto é trocada à parte, pois é opcional na alteração. */
    public void atualizar(String nome, String descricao, int custoMoedas) {
        this.nome = nome;
        this.descricao = descricao;
        this.custoMoedas = custoMoedas;
    }

    public void desativar() {
        this.ativa = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public int getCustoMoedas() {
        return custoMoedas;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public EmpresaParceira getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaParceira empresa) {
        this.empresa = empresa;
    }
}
