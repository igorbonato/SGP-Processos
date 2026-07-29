package br.jus.trt4.processo.mapper;

import br.jus.trt4.processo.domain.Parte;
import br.jus.trt4.processo.dto.response.ParteResponseDTO;
import org.mapstruct.Mapper;

// -------------------------------------------------------------------------------------------
// @Mapper(componentModel = "spring") — é isto que faz o MapStruct GERAR uma classe chamada
// "ParteMapperImpl" (em target/generated-sources/annotations) implementando esta interface, E
// anotar essa classe gerada com @Component — por isso conseguimos injetar "ParteMapper" via
// construtor em qualquer @Service, igual injetaríamos qualquer outro bean Spring, sem nunca
// escrever "new ParteMapperImpl()" em lugar nenhum.
//
// Só existe a direção Entity -> DTO aqui (não há "toEntity"). Ver o comentário completo em
// ProcessoMapper sobre por que o caminho de ESCRITA (DTO -> Entity) NÃO passa por um mapper
// neste projeto.
// -------------------------------------------------------------------------------------------
@Mapper(componentModel = "spring")
public interface ParteMapper {

    ParteResponseDTO toResponseDTO(Parte parte);
}
