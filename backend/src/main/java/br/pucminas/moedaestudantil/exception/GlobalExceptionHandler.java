package br.pucminas.moedaestudantil.exception;

import br.pucminas.moedaestudantil.dto.ErroDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Converte as exceções da aplicação em respostas JSON padronizadas ({@link ErroDTO}). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Set<String> CAMPO_OBRIGATORIO = Set.of("NotBlank", "NotNull");

    /** Uma mensagem por campo; "campo obrigatório" tem prioridade sobre erros de formato (ex.: CPF vazio). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            if (CAMPO_OBRIGATORIO.contains(erro.getCode())) {
                campos.put(erro.getField(), erro.getDefaultMessage());
            } else {
                campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
            }
        }
        return responder(HttpStatus.BAD_REQUEST, "Há campos inválidos no formulário.", campos);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDTO> tratarRegraNegocio(RegraNegocioException ex) {
        Map<String, String> campos = ex.getCampo() == null ? Map.of() : Map.of(ex.getCampo(), ex.getMessage());
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), campos);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição está mal formatado.", Map.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroDTO> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    }

    /** Negações dos @PreAuthorize: o usuário está autenticado, mas o registro não é dele. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroDTO> tratarAcessoNegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "Você não tem permissão para realizar esta operação.", Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroDTO> tratarArquivoGrande(MaxUploadSizeExceededException ex) {
        String mensagem = "A foto deve ter no máximo 5 MB.";
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, mensagem, Map.of("foto", mensagem));
    }

    /** Outra operação alterou o mesmo saldo ao mesmo tempo: bloqueio otimista da Conta ou deadlock detectado pelo banco. */
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErroDTO> tratarConflitoDeSaldo(ConcurrencyFailureException ex) {
        return responder(HttpStatus.CONFLICT, "O saldo foi alterado por outra operação. Confira o saldo e tente novamente.", Map.of());
    }

    /** Última barreira: restrições UNIQUE/FK do banco violadas por requisições concorrentes. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroDTO> tratarIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco de dados", ex);
        return responder(HttpStatus.CONFLICT, "A operação viola uma restrição do banco de dados (registro duplicado ou em uso).", Map.of());
    }

    private static ResponseEntity<ErroDTO> responder(HttpStatus status, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErroDTO(status.value(), mensagem, campos));
    }
}
