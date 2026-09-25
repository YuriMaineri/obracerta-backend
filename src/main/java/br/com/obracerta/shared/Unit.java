package br.com.obracerta.shared;

/**
 * Unidades observadas nos orcamentos reais da empresa.
 * LUMP_SUM (verba) representa servico cobrado por valor fechado, sem quantidade.
 */
public enum Unit {

    SQUARE_METER("m2"),
    METER("m"),
    PIECE("un"),
    POINT("ponto"),
    HOUR("h"),
    DAY("diaria"),
    LUMP_SUM("verba");

    /** Sigla exibida na proposta, em portugues. */
    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
