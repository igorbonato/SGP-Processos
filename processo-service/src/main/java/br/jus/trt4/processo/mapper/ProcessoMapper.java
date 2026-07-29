package br.jus.trt4.processo.mapper;

import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.dto.response.ProcessoResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * ---------------------------------------------------------------------------------------------
 * POR QUE ESTE MAPPER SÓ TEM O SENTIDO "ENTITY -> DTO" (leitura), NUNCA "DTO -> ENTITY" (escrita)?
 * ---------------------------------------------------------------------------------------------
 * {@link Processo} é um agregado rico (ver docs/03): não tem setters, seu único construtor
 * público exige numero/classeJudicial/vara/dataAutuacao, e o construtor sem argumentos (exigido
 * pelo JPA) é "protected" — inacessível a partir do pacote {@code mapper}. Mesmo que o MapStruct
 * tentasse gerar uma implementação "toEntity(ProcessoRequestDTO)", ele NÃO teria como popular
 * "partes"/"movimentacoes" corretamente, porque adicionar uma Parte/Movimentação exige passar
 * pelos métodos de domínio {@code adicionarParte}/{@code adicionarMovimentacao} — que existem
 * exatamente para não deixar ninguém (nem um mapper gerado) inserir um item na lista "por fora"
 * da regra de negócio.
 *
 * Por isso, a CRIAÇÃO de um Processo (ver {@code ProcessoServiceImpl.criar}) chama o construtor e
 * os métodos de domínio diretamente, em código Java explícito — nenhum mapper participa da
 * escrita. MapStruct entra em cena só para o caminho de LEITURA (Entity -> DTO), que é
 * mapeamento de dado puro, sem invariante nenhuma para proteger.
 *
 * Paralelo .NET: é a mesma razão pela qual, em um projeto DDD maduro, você raramente deixa o
 * AutoMapper "hidratar" um Aggregate Root diretamente a partir de um Command/DTO — o
 * `Map<Processo>(dto)` do AutoMapper teria o mesmíssimo problema de bypassar o construtor/métodos
 * de domínio. AutoMapper e MapStruct são ótimos para achatar dados de LEITURA; nenhum dos dois
 * deveria construir um agregado com invariantes por você.
 */
@Mapper(componentModel = "spring", uses = ParteMapper.class)
public interface ProcessoMapper {

    // -----------------------------------------------------------------------------------------
    // "partes" (List<Parte> em Processo, List<ParteResponseDTO> em ProcessoResponseDTO): o
    // MapStruct casa os dois campos pelo NOME e, para converter cada elemento, procura um método
    // Parte -> ParteResponseDTO — que ele encontra no ParteMapper declarado em "uses" acima, sem
    // precisarmos escrever mais nenhuma linha aqui.
    // -----------------------------------------------------------------------------------------
    ProcessoResponseDTO toResponseDTO(Processo processo);

    List<ProcessoResponseDTO> toResponseDTOList(List<Processo> processos);
}
