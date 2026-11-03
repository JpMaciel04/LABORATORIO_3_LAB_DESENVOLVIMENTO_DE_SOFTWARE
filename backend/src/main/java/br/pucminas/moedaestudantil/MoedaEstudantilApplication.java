package br.pucminas.moedaestudantil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** O login é feito por AutenticacaoService; o usuário padrão gerado pelo Spring Security não é usado. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MoedaEstudantilApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoedaEstudantilApplication.class, args);
    }
}
