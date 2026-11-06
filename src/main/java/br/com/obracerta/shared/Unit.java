package br.com.obracerta.shared;

/** Unidades de cobrança dos serviços e de compra dos insumos. */
public enum Unit {

    SQUARE_METER("m²"),
    METER("m"),
    CUBIC_METER("m³"),
    PIECE("un"),
    POINT("ponto"),
    HOUR("h"),
    DAY("diária"),
    LUMP_SUM("verba"),
    KILOGRAM("kg"),
    LITER("L"),
    BAG("saco"),
    CAN("lata"),
    ROLL("rolo"),
    BAR("barra"),
    BOX("caixa"),
    PACKAGE("pacote");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Só se compra inteiro: 2,3 sacos viram 3. Metro, m², litro e kg podem ser fracionados. */
    public boolean soldWhole() {
        return switch (this) {
            case PIECE, POINT, BAG, CAN, ROLL, BAR, BOX, PACKAGE -> true;
            default -> false;
        };
    }
}
