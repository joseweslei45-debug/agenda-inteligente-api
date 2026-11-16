package com.weslei.agenda.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.weslei.agenda.domain.Agendamento;
import com.weslei.agenda.domain.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Dois intervalos [a, b) e [c, d) se sobrepõem quando a < d e b > c.
     * O parâmetro ignorarId permite reagendar sem conflitar consigo mesmo.
     */
    @Query("""
            select count(a) > 0 from Agendamento a
            where a.profissional.id = :profissionalId
              and a.status = com.weslei.agenda.domain.StatusAgendamento.CONFIRMADO
              and a.inicio < :fim
              and a.fim > :inicio
              and (:ignorarId is null or a.id <> :ignorarId)
            """)
    boolean existeConflito(@Param("profissionalId") Long profissionalId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim,
                           @Param("ignorarId") Long ignorarId);

    List<Agendamento> findByProfissionalIdAndStatusAndInicioBetweenOrderByInicio(
            Long profissionalId, StatusAgendamento status, LocalDateTime de, LocalDateTime ate);
}
