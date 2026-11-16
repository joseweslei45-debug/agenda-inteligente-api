package com.weslei.agenda.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Profissional profissional;

    @ManyToOne(optional = false)
    private Servico servico;

    @Column(nullable = false)
    private String cliente;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.CONFIRMADO;

    private String motivoCancelamento;

    protected Agendamento() {
    }

    public Agendamento(Profissional profissional, Servico servico, String cliente, LocalDateTime inicio) {
        this.profissional = profissional;
        this.servico = servico;
        this.cliente = cliente;
        this.inicio = inicio;
        this.fim = inicio.plusMinutes(servico.getDuracaoMinutos());
    }

    public void cancelar(String motivo) {
        this.status = StatusAgendamento.CANCELADO;
        this.motivoCancelamento = motivo;
    }

    public void reagendar(LocalDateTime novoInicio) {
        this.inicio = novoInicio;
        this.fim = novoInicio.plusMinutes(servico.getDuracaoMinutos());
    }

    public boolean isConfirmado() {
        return status == StatusAgendamento.CONFIRMADO;
    }

    public Long getId() { return id; }
    public Profissional getProfissional() { return profissional; }
    public Servico getServico() { return servico; }
    public String getCliente() { return cliente; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public StatusAgendamento getStatus() { return status; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
}
