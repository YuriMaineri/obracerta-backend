package br.com.obracerta.shared;

/**
 * Regime de material: quem fornece o material e como ele entra no orcamento.
 * As quatro formas foram observadas nos orcamentos reais da empresa.
 */
public enum MaterialSupply {

    /** Material por conta do cliente; nao entra no valor. */
    CUSTOMER_SUPPLIED,

    /** Material listado item a item, com preco. */
    ITEMIZED,

    /** Valor aproximado informado; lista detalhada apos a aprovacao. */
    ESTIMATED,

    /** Empresa compra e apresenta as notas durante a obra. */
    BY_RECEIPT
}
