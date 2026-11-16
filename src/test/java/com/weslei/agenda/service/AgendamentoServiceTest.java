package com.weslei.agenda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weslei.agenda.domain.Agendamento;
import com.weslei.agenda.domain.Profissional;
import com.weslei.agenda.domain.Servico;
import com.weslei.agenda.domain.StatusAgendamento;
import com.weslei.agenda.repository.AgendamentoRepository;
import com.weslei.agenda.repository.ProfissionalRepository;
import com.weslei.agenda.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    /** "Agora" fixo: segunda-feira, 30/11/2026 às 08:00. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 30, 8, 0);
    private static final LocalDate SEGUNDA = AGORA.toLocalDate();

    @Mock
    private AgendamentoRepository agendamentos;
    @Mock
    private ProfissionalRepository profissionais;
    @Mock
    private ServicoRepository servicos;

    private AgendamentoService service;
    private Profissional ana;
    private Servico corte;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);
        service = new AgendamentoService(agendamentos, profissionais, servicos, clock);
        ana = new Profissional("Ana", "Cabeleireira",
                LocalTime.of(8, 0), LocalTime.of(18, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        corte = new Servico("Corte", 30, new BigDecimal("35.00"));
    }

    private void catalogo() {
        when(profissionais.findById(1L)).thenReturn(Optional.of(ana));
        when(servicos.findById(1L)).thenReturn(Optional.of(corte));
    }

    @Test
    void deveAgendarHorarioLivre() {
        catalogo();
        when(agendamentos.existeConflito(any(), any(), any(), any())).thenReturn(false);
        when(agendamentos.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Agendamento criado = service.agendar(1L, 1L, "Maria", SEGUNDA.atTime(10, 0));

        assertEquals(SEGUNDA.atTime(10, 30), criado.getFim());
        assertTrue(criado.isConfirmado());
    }

    @Test
    void deveRecusarHorarioEmConflito() {
        catalogo();
        when(agendamentos.existeConflito(any(), any(), any(), any())).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, 1L, "Maria", SEGUNDA.atTime(10, 0)));

        assertTrue(erro.getMessage().contains("conflita"));
    }

    @Test
    void deveRecusarHorarioQueInvadeAlmoco() {
        catalogo();
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, 1L, "Maria", SEGUNDA.atTime(11, 45)));
    }

    @Test
    void deveRecusarAgendamentoSemAntecedenciaMinima() {
        catalogo();
        assertThrows(RegraNegocioException.class,
                () -> service.agendar(1L, 1L, "Maria", SEGUNDA.atTime(8, 30)));
    }

    @Test
    void deveRecusarDiaSemAtendimento() {
        catalogo();
        LocalDateTime sabado = LocalDateTime.of(2026, 12, 5, 10, 0);
        assertThrows(RegraNegocioException.class, () -> service.agendar(1L, 1L, "Maria", sabado));
    }

    @Test
    void deveRecusarCancelamentoForaDoPrazo() {
        Agendamento proximo = new Agendamento(ana, corte, "Maria", SEGUNDA.atTime(9, 30));
        when(agendamentos.findById(5L)).thenReturn(Optional.of(proximo));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(5L, "Imprevisto"));
        assertTrue(proximo.isConfirmado());
    }

    @Test
    void devePermitirCancelamentoDentroDoPrazo() {
        Agendamento tarde = new Agendamento(ana, corte, "Maria", SEGUNDA.atTime(14, 0));
        when(agendamentos.findById(5L)).thenReturn(Optional.of(tarde));

        Agendamento cancelado = service.cancelar(5L, "Imprevisto");

        assertEquals(StatusAgendamento.CANCELADO, cancelado.getStatus());
        assertEquals("Imprevisto", cancelado.getMotivoCancelamento());
    }

    @Test
    void deveCalcularHorariosDisponiveis() {
        catalogo();
        Agendamento existente = new Agendamento(ana, corte, "João", SEGUNDA.atTime(10, 0));
        when(agendamentos.findByProfissionalIdAndStatusAndInicioBetweenOrderByInicio(
                any(), eq(StatusAgendamento.CONFIRMADO), any(), any()))
                .thenReturn(List.of(existente));

        List<LocalDateTime> livres = service.horariosDisponiveis(1L, 1L, SEGUNDA);

        // respeita a antecedência mínima de 1h
        assertFalse(livres.contains(SEGUNDA.atTime(8, 30)));
        assertTrue(livres.contains(SEGUNDA.atTime(9, 0)));
        // não sobrepõe o agendamento das 10:00
        assertTrue(livres.contains(SEGUNDA.atTime(9, 30)));
        assertFalse(livres.contains(SEGUNDA.atTime(9, 45)));
        assertFalse(livres.contains(SEGUNDA.atTime(10, 0)));
        assertTrue(livres.contains(SEGUNDA.atTime(10, 30)));
        // respeita o intervalo de almoço
        assertFalse(livres.contains(SEGUNDA.atTime(11, 45)));
        assertFalse(livres.contains(SEGUNDA.atTime(12, 30)));
        assertTrue(livres.contains(SEGUNDA.atTime(13, 0)));
        // último horário que cabe no expediente
        assertTrue(livres.contains(SEGUNDA.atTime(17, 30)));
        assertFalse(livres.contains(SEGUNDA.atTime(17, 45)));
    }
}
