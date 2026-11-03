package br.pucminas.moedaestudantil.dto;

import java.util.Locale;

/** Normaliza os dados recebidos antes de virarem entidades, para que as verificações de unicidade comparem a forma canônica. */
final class Formatos {

    private Formatos() {
    }

    /** "123.456.789-09" -> "12345678909" (CPF, CNPJ e CEP são armazenados sem máscara). */
    static String apenasDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    static String email(String valor) {
        return valor == null ? null : valor.trim().toLowerCase(Locale.ROOT);
    }

    static String texto(String valor) {
        return valor == null ? null : valor.trim();
    }

    /** Campo opcional: texto em branco é tratado como ausente. */
    static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
