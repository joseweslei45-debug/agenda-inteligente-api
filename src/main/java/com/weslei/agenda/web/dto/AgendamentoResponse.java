package com.weslei.agenda.web.dto;

import java.time.LocalDateTime;

import com.weslei.agenda.domain.Agendamento;
import com.weslei.agenda.domain.StatusAgendamento;

public record AgendamentoResponse(
        Long id,
        String profissional,
        String servico,
        String cliente,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status,
        String motivoCancelamento) {

    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getProfissional().getNome(),
                a.getServico().getNome(),
                a.getCliente(),
                a.getInicio(),
                a.getFim(),
                a.getStatus(),
                a.getMotivoCancelamento());
    }
}
