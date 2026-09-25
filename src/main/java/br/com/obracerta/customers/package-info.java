/**
 * Cadastro de clientes e obras.
 *
 * <p>Os clientes da empresa sao em boa parte pessoas juridicas (condominios,
 * cartorios, distribuidoras), com CNPJ e pessoa de contato ("A/C"). O cadastro
 * precisa atender tanto PF quanto PJ.
 *
 * <p>Modulo folha: nao depende de nenhum outro modulo de negocio.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Customers",
        allowedDependencies = {"shared"})
package br.com.obracerta.customers;
