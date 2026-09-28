package br.com.obracerta.company;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClauseRequest(

        @NotNull(message = "Tipo da clausula e obrigatorio")
        ClauseType type,

        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 150, message = "Titulo com no maximo 150 caracteres")
        String title,

        @NotBlank(message = "Texto da clausula e obrigatorio")
        @Size(max = 2000, message = "Texto com no maximo 2000 caracteres")
        String content,

        boolean defaultClause,

        @Min(value = 0, message = "Ordem nao pode ser negativa")
        Integer sortOrder) {
}
