package com.weslei.agenda.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NovoAgendamentoRequest(
        @NotNull Long profissionalId,
        @NotNull Long servicoId,
        @NotBlank String cliente,
        @NotNull LocalDateTime inicio) {
}
