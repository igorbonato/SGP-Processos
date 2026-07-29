/**
 * Modelo de domínio (entidades JPA) do processo-service: {@link br.jus.trt4.processo.domain.Processo}
 * (agregado raiz), {@link br.jus.trt4.processo.domain.Parte} e
 * {@link br.jus.trt4.processo.domain.Movimentacao} (entidades filhas), mais os enums que
 * governam seus estados.
 *
 * "Modelo rico": as invariantes de negócio (ex.: processo arquivado não aceita movimentação)
 * moram nos métodos das próprias entidades, não em uma camada de serviço externa — mesma
 * filosofia de agregado DDD que você já aplica no .NET, só que aqui as entidades também carregam
 * as anotações de persistência (JPA) — não há uma separação de "entidade de domínio pura" vs
 * "entidade de persistência" neste projeto de estudo (isso existiria em uma variação Clean
 * Architecture/Onion, ver nota final de docs/04-mapeamento-dotnet-java.md).
 */
package br.jus.trt4.processo.domain;
