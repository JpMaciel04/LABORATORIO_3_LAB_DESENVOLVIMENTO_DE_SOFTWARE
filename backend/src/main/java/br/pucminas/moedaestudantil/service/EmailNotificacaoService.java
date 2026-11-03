package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Vantagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envia as notificações por SMTP (RN07, RN11). O texto é montado durante a transação e a entrega ocorre em
 * segundo plano, depois da confirmação. Sem servidor SMTP configurado, as mensagens são escritas no log.
 */
@Service
public class EmailNotificacaoService implements NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacaoService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final TaskExecutor executor;
    private final String remetente;

    public EmailNotificacaoService(ObjectProvider<JavaMailSender> mailSender,
                                   @Qualifier("applicationTaskExecutor") TaskExecutor executor,
                                   @Value("${app.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.executor = executor;
        this.remetente = remetente;
    }

    @Override
    public void notificarRecebimentoMoedas(EnvioMoedas envio, Professor professor, Aluno aluno) {
        enviar(aluno.getEmail(), "Você recebeu " + envio.getValor() + " moedas", """
                Olá, %s!

                O(a) professor(a) %s enviou %d moedas para você.

                Motivo: %s

                Consulte seu saldo e as vantagens disponíveis no Sistema de Moeda Estudantil.
                """.formatted(aluno.getNome(), professor.getNome(), envio.getValor(), envio.getMotivo()));
    }

    @Override
    public void enviarCupomAluno(ResgateVantagem resgate, Aluno aluno) {
        Vantagem vantagem = resgate.getVantagem();
        enviar(aluno.getEmail(), "Seu cupom de resgate", """
                Olá, %s!

                Seu resgate foi confirmado.

                Vantagem: %s
                Empresa parceira: %s
                Custo: %d moedas
                Código do cupom: %s

                Apresente este código na troca presencial.
                """.formatted(aluno.getNome(), vantagem.getNome(), vantagem.getEmpresa().getNome(),
                resgate.getValor(), resgate.getCodigoCupom()));
    }

    @Override
    public void notificarEmpresaParceira(ResgateVantagem resgate, Aluno aluno) {
        EmpresaParceira empresa = resgate.getVantagem().getEmpresa();
        enviar(empresa.getEmail(), "Nova vantagem resgatada", """
                Olá, %s!

                O(a) aluno(a) %s resgatou a vantagem "%s".

                Código para conferência: %s

                Confira este código na troca presencial.
                """.formatted(empresa.getNome(), aluno.getNome(), resgate.getVantagem().getNome(),
                resgate.getCodigoCupom()));
    }

    private void enviar(String destinatario, String assunto, String texto) {
        AposCommit.executar(() -> executor.execute(() -> entregar(destinatario, assunto, texto)));
    }

    private void entregar(String destinatario, String assunto, String texto) {
        JavaMailSender servidor = mailSender.getIfAvailable();
        if (servidor == null) {
            log.info("SMTP não configurado. E-mail para {}\nAssunto: {}\n{}", destinatario, assunto, texto);
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        try {
            servidor.send(mensagem);
        } catch (MailException e) {
            log.error("Falha ao enviar o e-mail \"{}\" para {}", assunto, destinatario, e);
        }
    }
}
