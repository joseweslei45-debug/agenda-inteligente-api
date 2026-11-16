package com.weslei.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weslei.agenda.domain.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
