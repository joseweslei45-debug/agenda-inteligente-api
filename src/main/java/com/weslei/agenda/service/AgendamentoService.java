package com.weslei.agenda.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weslei.agenda.domain.Agendamento;
import com.weslei.agenda.domain.Profissional;
import com.weslei.agenda.domain.Servico;
import com.weslei.agenda.domain.StatusAgendamento;
import com.weslei.agenda.repository.AgendamentoRepository;
import com.weslei.agenda.repository.ProfissionalRepository;
import com.weslei.agenda.repository.ServicoRepository;

/**
 * Concentra as regras de negócio da agenda.
 */
@Service
public class AgendamentoService {

    static final Duration ANTECEDENCIA_MINIMA = Duration.ofHours(1);
    static final Duration PRAZO_CANCELAMENTO = Duration.ofHours(2);
    static final int PASSO_GRADE_MINUTOS = 15;

    private final AgendamentoRepository agendamentos;
    private final ProfissionalRepository profissionais;
    private final ServicoRepository servicos;
    private final Clock clock;

    public AgendamentoService(AgendamentoRepository agendamentos,
                              ProfissionalRepository profissionais,
                              ServicoRepository servicos,
                              Clock clock) {
        this.agendamentos = agendamentos;
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.clock = clock;
    }

    @Transactional
    public Agendamento agendar(Long profissionalId, Long servicoId, String cliente, LocalDateTime inicio) {
        Profissional profissional = buscarProfissional(profissionalId);
        Servico servico = buscarServico(servicoId);
        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());

        validarHorario(profissional, inicio, fim, null);

        return agendamentos.save(new Agendamento(profissional, servico, cliente, inicio));
    }

    @Transactional
    public Agendamento cancelar(Long id, String motivo) {
        Agendamento agendamento = buscar(id);
        if (!agendamento.isConfirmado()) {
            throw new RegraNegocioException("Agendamento já está cancelado");
        }
        if (agora().plus(PRAZO_CANCELAMENTO).isAfter(agendamento.getInicio())) {
            throw new RegraNegocioException("Cancelamentos devem ser feitos com pelo menos 2h de antecedência");
        }
        agendamento.cancelar(motivo);
        return agendamento;
    }

    @Transactional
    public Agendamento reagendar(Long id, LocalDateTime novoInicio) {
        Agendamento agendamento = buscar(id);
        if (!agendamento.isConfirmado()) {
            throw new RegraNegocioException("Não é possível reagendar um agendamento cancelado");
        }
        LocalDateTime novoFim = novoInicio.plusMinutes(agendamento.getServico().getDuracaoMinutos());
        validarHorario(agendamento.getProfissional(), novoInicio, novoFim, agendamento.getId());
        agendamento.reagendar(novoInicio);
        return agendamento;
    }

    @Transactional(readOnly = true)
    public Agendamento buscar(Long id) {
        return agendamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Agendamento> agendaDoDia(Long profissionalId, LocalDate data) {
        buscarProfissional(profissionalId);
        return agendamentos.findByProfissionalIdAndStatusAndInicioBetweenOrderByInicio(
                profissionalId, StatusAgendamento.CONFIRMADO, data.atStartOfDay(), data.atTime(LocalTime.MAX));
    }

    /**
     * Percorre o expediente em blocos de 15 minutos e devolve os inícios
     * em que o serviço cabe sem violar nenhuma regra.
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> horariosDisponiveis(Long profissionalId, Long servicoId, LocalDate data) {
        Profissional profissional = buscarProfissional(profissionalId);
        Servico servico = buscarServico(servicoId);
        List<Agendamento> ocupados = agendaDoDia(profissionalId, data);
        LocalDateTime limite = agora().plus(ANTECEDENCIA_MINIMA);
        LocalDateTime fimExpediente = data.atTime(profissional.getFimExpediente());

        List<LocalDateTime> livres = new ArrayList<>();
        LocalDateTime slot = data.atTime(profissional.getInicioExpediente());
        while (!slot.plusMinutes(servico.getDuracaoMinutos()).isAfter(fimExpediente)) {
            LocalDateTime inicioSlot = slot;
            LocalDateTime fimSlot = slot.plusMinutes(servico.getDuracaoMinutos());
            boolean ocupado = ocupados.stream()
                    .anyMatch(a -> a.getInicio().isBefore(fimSlot) && a.getFim().isAfter(inicioSlot));

            if (!inicioSlot.isBefore(limite) && profissional.atendeEm(inicioSlot, fimSlot) && !ocupado) {
                livres.add(inicioSlot);
            }
            slot = slot.plusMinutes(PASSO_GRADE_MINUTOS);
        }
        return livres;
    }

    private void validarHorario(Profissional profissional, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        if (inicio.isBefore(agora().plus(ANTECEDENCIA_MINIMA))) {
            throw new RegraNegocioException("Agendamentos exigem pelo menos 1h de antecedência");
        }
        if (!profissional.atendeEm(inicio, fim)) {
            throw new RegraNegocioException("Horário fora da jornada de atendimento do profissional");
        }
        if (agendamentos.existeConflito(profissional.getId(), inicio, fim, ignorarId)) {
            throw new RegraNegocioException("Horário indisponível: conflita com outro agendamento");
        }
    }

    private Profissional buscarProfissional(Long id) {
        return profissionais.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado"));
    }

    private Servico buscarServico(Long id) {
        return servicos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço " + id + " não encontrado"));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
