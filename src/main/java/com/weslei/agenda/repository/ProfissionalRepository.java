package com.weslei.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weslei.agenda.domain.Profissional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
}
