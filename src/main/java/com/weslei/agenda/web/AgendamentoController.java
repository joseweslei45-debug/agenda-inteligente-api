package com.weslei.agenda.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.weslei.agenda.domain.Agendamento;
import com.weslei.agenda.service.AgendamentoService;
import com.weslei.agenda.web.dto.AgendamentoResponse;
import com.weslei.agenda.web.dto.CancelamentoRequest;
import com.weslei.agenda.web.dto.NovoAgendamentoRequest;
import com.weslei.agenda.web.dto.ReagendamentoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody NovoAgendamentoRequest req,
                                                     UriComponentsBuilder uriBuilder) {
        Agendamento criado = service.agendar(req.profissionalId(), req.servicoId(), req.cliente(), req.inicio());
        URI location = uriBuilder.path("/api/agendamentos/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(AgendamentoResponse.de(criado));
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.buscar(id));
    }

    @GetMapping
    public List<AgendamentoResponse> agendaDoDia(@RequestParam Long profissionalId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agendaDoDia(profissionalId, data).stream().map(AgendamentoResponse::de).toList();
    }

    @PatchMapping("/{id}/cancelamento")
    public AgendamentoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelamentoRequest req) {
        return AgendamentoResponse.de(service.cancelar(id, req.motivo()));
    }

    @PatchMapping("/{id}/reagendamento")
    public AgendamentoResponse reagendar(@PathVariable Long id, @Valid @RequestBody ReagendamentoRequest req) {
        return AgendamentoResponse.de(service.reagendar(id, req.novoInicio()));
    }
}
