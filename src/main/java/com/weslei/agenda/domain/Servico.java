package com.weslei.agenda.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int duracaoMinutos;

    @Column(nullable = false)
    private BigDecimal preco;

    protected Servico() {
    }

    public Servico(String nome, int duracaoMinutos, BigDecimal preco) {
        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("Duração deve ser positiva");
        }
        this.nome = nome;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public BigDecimal getPreco() { return preco; }
}
