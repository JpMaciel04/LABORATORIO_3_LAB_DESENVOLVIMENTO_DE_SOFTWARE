package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Base de Aluno, Professor e EmpresaParceira: concentra o login (e-mail) e a senha (RN12).
 * Herança mapeada com JOINED: tabela "usuario" com os dados comuns e uma tabela por subtipo.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** Hash BCrypt da senha (60 caracteres); a senha em texto puro nunca é persistida. */
    @Column(nullable = false, length = 60)
    private String senha;

    /** Exclusão lógica: preserva o histórico de transações e resgates ligados ao usuário. */
    @Column(nullable = false)
    private boolean ativo = true;

    public void desativar() {
        this.ativo = false;
    }

    /** Define as funcionalidades que o usuário pode acessar depois do login. */
    public abstract Perfil getPerfil();

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
