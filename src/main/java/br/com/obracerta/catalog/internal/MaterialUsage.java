package br.com.obracerta.catalog.internal;

/** Um insumo usado na composição de um serviço ativo. */
public record MaterialUsage(Long materialId, String serviceName) {
}
