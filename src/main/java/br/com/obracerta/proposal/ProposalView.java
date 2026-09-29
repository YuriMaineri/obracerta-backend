package br.com.obracerta.proposal;

import java.util.List;

/** Dados já formatados para o template da proposta. Não contém nada da visão interna. */
record ProposalView(
        String logoDataUri,
        String companyName,
        String companyTaxId,
        String companyPhone,
        String customerName,
        String customerDocument,
        List<String> customerAddress,
        String customerContact,
        String title,
        String number,
        String issueDate,
        List<String> services,
        List<Item> materialItems,
        String materialTotal,
        String materialNote,
        List<Item> priceItems,
        String grandTotal,
        String leadTime,
        List<String> payment,
        List<String> warranty,
        List<String> observations,
        List<String> regulatory,
        String placeAndDate,
        String signatory) {

    /** Linha com descrição e valor já formatado (o valor pode faltar). */
    record Item(String description, String amount) {
    }
}
