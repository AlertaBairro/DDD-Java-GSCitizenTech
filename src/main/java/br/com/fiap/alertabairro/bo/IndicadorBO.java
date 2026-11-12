package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dto.ContagemResponse;
import br.com.fiap.alertabairro.dto.IndicadoresResponse;
import br.com.fiap.alertabairro.dto.ReincidenciaResponse;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoProblema;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Indicadores de impacto para o gestor da subprefeitura.
 */
@Service
public class IndicadorBO {

    private final OcorrenciaDAO ocorrenciaDAO;
    private final BairroDAO bairroDAO;
    private final TipoProblemaDAO tipoProblemaDAO;
    private final UsuarioBO usuarioBO;

    public IndicadorBO(OcorrenciaDAO ocorrenciaDAO, BairroDAO bairroDAO, TipoProblemaDAO tipoProblemaDAO,
                       UsuarioBO usuarioBO) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.bairroDAO = bairroDAO;
        this.tipoProblemaDAO = tipoProblemaDAO;
        this.usuarioBO = usuarioBO;
    }

    public IndicadoresResponse obterIndicadores(Long usuarioId) {
        usuarioBO.autenticarComPerfil(usuarioId, Perfil.GESTOR, "ver os indicadores de impacto");
        return calcular(ocorrenciaDAO.listarTodas(), bairroDAO.listarTodos(), tipoProblemaDAO.listarTodos());
    }

    /** Calcula os indicadores a partir das ocorrencias (separado do acesso ao banco para facilitar os testes). */
    public IndicadoresResponse calcular(List<Ocorrencia> ocorrencias, List<Bairro> bairros, List<TipoProblema> tipos) {
        long total = ocorrencias.size();
        long resolvidas = ocorrencias.stream().filter(Ocorrencia::isResolvida).count();
        long emAberto = total - resolvidas;
        long alertas = ocorrencias.stream().filter(Ocorrencia::isAlagamento).count();
        long alertasAtivos = ocorrencias.stream().filter(o -> o.isAlagamento() && !o.isResolvida()).count();

        Double mediaHoras = tempoMedioResolucaoHoras(ocorrencias);

        return new IndicadoresResponse(
                total,
                emAberto,
                resolvidas,
                mediaHoras == null ? null : Math.round(mediaHoras * 10.0) / 10.0,
                formatarHoras(mediaHoras),
                alertas,
                alertasAtivos,
                contarPorBairro(ocorrencias, bairros),
                contarPorTipo(ocorrencias, tipos),
                contarPorStatus(ocorrencias),
                reincidencias(ocorrencias));
    }

    /** Media de horas entre registro e resolucao, considerando somente as ocorrencias resolvidas. */
    Double tempoMedioResolucaoHoras(List<Ocorrencia> ocorrencias) {
        List<Duration> duracoes = ocorrencias.stream()
                .filter(o -> o.isResolvida() && o.getDataCriacao() != null && o.getDataResolucao() != null)
                .map(o -> Duration.between(o.getDataCriacao(), o.getDataResolucao()))
                .filter(d -> !d.isNegative())
                .toList();
        if (duracoes.isEmpty()) {
            return null;
        }
        double somaMinutos = duracoes.stream().mapToLong(Duration::toMinutes).sum();
        return somaMinutos / duracoes.size() / 60.0;
    }

    /** Combinacoes bairro + tipo com mais de uma ocorrencia, da maior para a menor contagem. */
    List<ReincidenciaResponse> reincidencias(List<Ocorrencia> ocorrencias) {
        Map<String, List<Ocorrencia>> grupos = ocorrencias.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getBairro().getId() + "|" + o.getTipo().getCodigo(),
                        LinkedHashMap::new, Collectors.toList()));

        return grupos.values().stream()
                .filter(grupo -> grupo.size() > 1)
                .map(grupo -> {
                    Ocorrencia exemplo = grupo.get(0);
                    return new ReincidenciaResponse(exemplo.getBairro().getId(), exemplo.getBairro().getNome(),
                            exemplo.getTipo().getCodigo(), exemplo.getTipo().getNome(), grupo.size());
                })
                .sorted(Comparator.comparingLong(ReincidenciaResponse::total).reversed()
                        .thenComparing(ReincidenciaResponse::bairro)
                        .thenComparing(ReincidenciaResponse::tipo))
                .toList();
    }

    private List<ContagemResponse> contarPorBairro(List<Ocorrencia> ocorrencias, List<Bairro> bairros) {
        Map<Long, Long> contagem = ocorrencias.stream()
                .collect(Collectors.groupingBy(o -> o.getBairro().getId(), Collectors.counting()));
        // inclui todos os bairros cadastrados, mesmo os sem ocorrencias
        Map<Long, String> nomes = new LinkedHashMap<>();
        bairros.forEach(b -> nomes.put(b.getId(), b.getNome()));
        ocorrencias.forEach(o -> nomes.putIfAbsent(o.getBairro().getId(), o.getBairro().getNome()));

        return nomes.entrySet().stream()
                .map(e -> new ContagemResponse(String.valueOf(e.getKey()), e.getValue(),
                        contagem.getOrDefault(e.getKey(), 0L)))
                .sorted(Comparator.comparingLong(ContagemResponse::total).reversed()
                        .thenComparing(ContagemResponse::nome))
                .toList();
    }

    private List<ContagemResponse> contarPorTipo(List<Ocorrencia> ocorrencias, List<TipoProblema> tipos) {
        Map<String, Long> contagem = ocorrencias.stream()
                .collect(Collectors.groupingBy(o -> o.getTipo().getCodigo(), Collectors.counting()));
        Map<String, String> nomes = new LinkedHashMap<>();
        tipos.forEach(t -> nomes.put(t.getCodigo(), t.getNome()));
        ocorrencias.forEach(o -> nomes.putIfAbsent(o.getTipo().getCodigo(), o.getTipo().getNome()));

        return nomes.entrySet().stream()
                .map(e -> new ContagemResponse(e.getKey(), e.getValue(), contagem.getOrDefault(e.getKey(), 0L)))
                .sorted(Comparator.comparingLong(ContagemResponse::total).reversed()
                        .thenComparing(ContagemResponse::nome))
                .toList();
    }

    private List<ContagemResponse> contarPorStatus(List<Ocorrencia> ocorrencias) {
        Map<StatusOcorrencia, Long> contagem = ocorrencias.stream()
                .map(Ocorrencia::getStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return Arrays.stream(StatusOcorrencia.values())
                .map(s -> new ContagemResponse(s.name(), s.getDescricao(), contagem.getOrDefault(s, 0L)))
                .toList();
    }

    /** Ex.: 50.0 -> "2 d 2 h"; 5.5 -> "5 h 30 min"; 0.25 -> "15 min". */
    static String formatarHoras(Double horas) {
        if (horas == null) {
            return "Sem ocorrências resolvidas";
        }
        long minutosTotais = Math.round(horas * 60);
        long dias = minutosTotais / (24 * 60);
        long horasRestantes = (minutosTotais % (24 * 60)) / 60;
        long minutos = minutosTotais % 60;
        if (dias > 0) {
            return horasRestantes > 0 ? dias + " d " + horasRestantes + " h" : dias + " d";
        }
        if (horasRestantes > 0) {
            return minutos > 0 ? horasRestantes + " h " + minutos + " min" : horasRestantes + " h";
        }
        return minutos + " min";
    }
}
