package br.com.fiap.alertabairro.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Tratamento centralizado de excecoes.
 * Converte as excecoes da aplicacao em respostas JSON padronizadas
 * (timestamp, status, erro, mensagem, caminho) com o codigo HTTP correto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    // ---------------------------------------------------------------
    // Excecoes proprias da aplicacao
    // ---------------------------------------------------------------

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(ValidacaoException e, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResposta> tratarNaoAutenticado(NaoAutenticadoException e, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), req, null);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(AcessoNegadoException e, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage(), req, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(ConflitoException e, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException e, HttpServletRequest req) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), req, null);
    }

    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroResposta> tratarPersistencia(PersistenciaException e, HttpServletRequest req) {
        LOG.error("Erro de banco de dados em {}: {}", req.getRequestURI(), e.getMessage(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao acessar o banco de dados. Tente novamente em instantes.", req, null);
    }

    // ---------------------------------------------------------------
    // Excecoes do Spring MVC / Bean Validation
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarBeanValidation(MethodArgumentNotValidException e,
                                                             HttpServletRequest req) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted()
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", req, detalhes);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResposta> tratarValidacaoDeParametros(HandlerMethodValidationException e,
                                                                    HttpServletRequest req) {
        List<String> detalhes = e.getAllErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", req, detalhes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> tratarConstraintViolation(ConstraintViolationException e,
                                                                  HttpServletRequest req) {
        List<String> detalhes = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", req, detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoInvalido(HttpMessageNotReadableException e,
                                                            HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou com JSON inválido (verifique tipos e valores de enums).", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoInvalido(MethodArgumentTypeMismatchException e,
                                                           HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "': " + e.getValue(), req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroAusente(MissingServletRequestParameterException e,
                                                               HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + e.getParameterName(), req, null);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResposta> tratarHeaderAusente(MissingRequestHeaderException e,
                                                            HttpServletRequest req) {
        if (HEADER_USUARIO.equalsIgnoreCase(e.getHeaderName())) {
            return responder(HttpStatus.UNAUTHORIZED,
                    "Faça login e envie o header " + HEADER_USUARIO + " com o id do usuário.", req, null);
        }
        return responder(HttpStatus.BAD_REQUEST, "Header obrigatório ausente: " + e.getHeaderName(), req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + e.getMethod() + " não suportado nesta rota.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMediaType(HttpMediaTypeNotSupportedException e,
                                                        HttpServletRequest req) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Envie o corpo da requisição como application/json.", req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarRotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada.", req, null);
    }

    // ---------------------------------------------------------------
    // Qualquer outro erro inesperado
    // ---------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception e, HttpServletRequest req) {
        LOG.error("Erro inesperado em {}", req.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente em instantes.", req, null);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String mensagem, HttpServletRequest req,
                                                   List<String> detalhes) {
        ErroResposta corpo = new ErroResposta(
                OffsetDateTime.now(FUSO),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                req.getRequestURI(),
                detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
