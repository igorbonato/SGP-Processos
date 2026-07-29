/**
 * Interfaces de mapeamento MapStruct (Entity -> DTO). Nenhuma classe de implementação é escrita
 * aqui — o annotation processor mapstruct-processor gera uma "*Impl" para cada interface em
 * tempo de compilação (ver configuração em processo-service/pom.xml). Ver
 * {@link br.jus.trt4.processo.mapper.ProcessoMapper} para o porquê deste pacote só cobrir o
 * sentido de leitura (Entity -> DTO), nunca escrita (DTO -> Entity).
 */
package br.jus.trt4.processo.mapper;
