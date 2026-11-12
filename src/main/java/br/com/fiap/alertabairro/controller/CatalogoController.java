package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.CatalogoBO;
import br.com.fiap.alertabairro.dto.BairroResponse;
import br.com.fiap.alertabairro.dto.TipoOcorrenciaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Bairros e tipos", description = "Cadastros básicos do modelo do grupo (públicos)")
public class CatalogoController {

    private final CatalogoBO catalogoBO;

    public CatalogoController(CatalogoBO catalogoBO) {
        this.catalogoBO = catalogoBO;
    }

    @GetMapping("/bairros")
    @Operation(summary = "Lista os bairros (T_AB_BAIRRO)")
    public ResponseEntity<List<BairroResponse>> listarBairros() {
        return ResponseEntity.ok(catalogoBO.listarBairros());
    }

    @GetMapping("/bairros/{id}")
    @Operation(summary = "Busca um bairro pelo id")
    public ResponseEntity<BairroResponse> buscarBairro(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoBO.buscarBairro(id));
    }

    @GetMapping("/tipos-ocorrencia")
    @Operation(summary = "Lista os tipos de ocorrência com o peso de risco (T_AB_TIPO_OCORRENCIA)")
    public ResponseEntity<List<TipoOcorrenciaResponse>> listarTipos() {
        return ResponseEntity.ok(catalogoBO.listarTipos());
    }

    @GetMapping("/tipos-ocorrencia/{id}")
    @Operation(summary = "Busca um tipo de ocorrência pelo id")
    public ResponseEntity<TipoOcorrenciaResponse> buscarTipo(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoBO.buscarTipo(id));
    }
}
