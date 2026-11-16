package com.weslei.agenda.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelamentoRequest(@NotBlank String motivo) {
}
