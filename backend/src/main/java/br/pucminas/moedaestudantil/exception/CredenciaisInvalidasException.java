package br.pucminas.moedaestudantil.exception;

/** Login recusado. A mensagem é sempre a mesma, para não revelar quais e-mails estão cadastrados (US01). */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos.");
    }
}
