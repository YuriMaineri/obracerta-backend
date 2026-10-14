/** Tipos transversais, abertos a todos os módulos. Só entra aqui o que é de fato compartilhado. */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN,
        displayName = "Shared")
package br.com.obracerta.shared;
