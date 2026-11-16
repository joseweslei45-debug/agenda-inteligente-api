package com.weslei.agenda.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weslei.agenda.domain.Profissional;
import com.weslei.agenda.domain.Servico;
import com.weslei.agenda.repository.ProfissionalRepository;
import com.weslei.agenda.repository.ServicoRepository;
import com.weslei.agenda.service.AgendamentoService;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final ProfissionalRepository profissionais;
    private final ServicoRepository servicos;
    private final AgendamentoService agendamentoService;

    public CatalogoController(ProfissionalRepository profissionais,
                              ServicoRepository servicos,
                              AgendamentoService agendamentoService) {
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/profissionais")
    public List<Profissional> listarProfissionais() {
        return profissionais.findAll();
    }

    @GetMapping("/servicos")
    public List<Servico> listarServicos() {
        return servicos.findAll();
    }

    @GetMapping("/profissionais/{id}/disponibilidade")
    public List<LocalDateTime> disponibilidade(@PathVariable Long id,
                                               @RequestParam Long servicoId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agendamentoService.horariosDisponiveis(id, servicoId, data);
    }
}
