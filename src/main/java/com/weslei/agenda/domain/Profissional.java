package com.weslei.agenda.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String especialidade;

    @Column(nullable = false)
    private LocalTime inicioExpediente;

    @Column(nullable = false)
    private LocalTime fimExpediente;

    private LocalTime inicioIntervalo;
    private LocalTime fimIntervalo;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> diasAtendimento = EnumSet.noneOf(DayOfWeek.class);

    protected Profissional() {
    }

    public Profissional(String nome, String especialidade,
                        LocalTime inicioExpediente, LocalTime fimExpediente,
                        LocalTime inicioIntervalo, LocalTime fimIntervalo,
                        Set<DayOfWeek> diasAtendimento) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.inicioExpediente = inicioExpediente;
        this.fimExpediente = fimExpediente;
        this.inicioIntervalo = inicioIntervalo;
        this.fimIntervalo = fimIntervalo;
        this.diasAtendimento = EnumSet.copyOf(diasAtendimento);
    }

    /**
     * Verifica se o intervalo [inicio, fim) cabe inteiro dentro da jornada:
     * mesmo dia, dia de atendimento, dentro do expediente e fora do intervalo.
     */
    public boolean atendeEm(LocalDateTime inicio, LocalDateTime fim) {
        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            return false;
        }
        if (!diasAtendimento.contains(inicio.getDayOfWeek())) {
            return false;
        }
        LocalTime i = inicio.toLocalTime();
        LocalTime f = fim.toLocalTime();
        if (i.isBefore(inicioExpediente) || f.isAfter(fimExpediente)) {
            return false;
        }
        boolean temIntervalo = inicioIntervalo != null && fimIntervalo != null;
        return !(temIntervalo && i.isBefore(fimIntervalo) && f.isAfter(inicioIntervalo));
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
    public LocalTime getInicioExpediente() { return inicioExpediente; }
    public LocalTime getFimExpediente() { return fimExpediente; }
    public LocalTime getInicioIntervalo() { return inicioIntervalo; }
    public LocalTime getFimIntervalo() { return fimIntervalo; }
    public Set<DayOfWeek> getDiasAtendimento() { return diasAtendimento; }
}
