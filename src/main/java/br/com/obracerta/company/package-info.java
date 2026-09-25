/**
 * Dados da propria empresa: identificacao, contas bancarias e clausulas padrao.
 *
 * <p>Existe porque os orcamentos reais repetem manualmente, a cada documento, os
 * dados bancarios, a garantia, as normas regulamentadoras e a clausula de servicos
 * extras. Nos documentos analisados os dados bancarios alternam entre dois bancos
 * sem criterio aparente, o que e exatamente o tipo de divergencia que o cadastro
 * unico elimina.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Company",
        allowedDependencies = {"shared"})
package br.com.obracerta.company;
