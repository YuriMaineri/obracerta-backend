package br.com.obracerta.shared;

/**
 * Modo de calculo: como o valor de um item de orcamento e obtido.
 *
 * <p>Os tres modos vieram da analise dos orcamentos reais: boa parte dos servicos
 * da empresa e cobrada por peca (23 arandelas, 15 lampadas) ou por verba fechada,
 * e nao por metragem. Suportar apenas medida atenderia menos da metade dos casos.
 */
public enum PricingMode {

    /** Quantidade derivada de uma medida informada (m2, m). */
    BY_MEASUREMENT,

    /** Quantidade informada em pecas ou pontos. */
    PER_PIECE,

    /** Valor fechado (verba), sem quantidade. */
    LUMP_SUM
}
