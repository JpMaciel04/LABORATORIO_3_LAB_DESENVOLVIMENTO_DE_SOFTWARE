package br.pucminas.moedaestudantil.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Empresa que oferece vantagens aos alunos em troca de moedas. */
@Entity
@Table(name = "empresa_parceira")
@DiscriminatorValue("EMPRESA")
public class EmpresaParceira extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vantagem> vantagens = new ArrayList<>();

    @Override
    public Perfil getPerfil() {
        return Perfil.EMPRESA;
    }

    /** Ao excluir a empresa, suas vantagens deixam de ser exibidas, mas o histórico de resgates é mantido. */
    @Override
    public void desativar() {
        super.desativar();
        vantagens.forEach(Vantagem::desativar);
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public List<Vantagem> getVantagens() {
        return vantagens;
    }
}
