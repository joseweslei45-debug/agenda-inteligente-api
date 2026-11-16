package com.weslei.agenda.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ReagendamentoRequest(@NotNull LocalDateTime novoInicio) {
}
