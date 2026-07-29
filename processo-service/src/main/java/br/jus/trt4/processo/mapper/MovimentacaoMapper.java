package br.jus.trt4.processo.mapper;

import br.jus.trt4.processo.domain.Movimentacao;
import br.jus.trt4.processo.dto.response.MovimentacaoResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovimentacaoMapper {

    MovimentacaoResponseDTO toResponseDTO(Movimentacao movimentacao);

    // MapStruct gera esta versão "em lista" sozinho, reaproveitando o método acima
    // elemento-a-elemento — não precisamos escrever nenhum loop.
    List<MovimentacaoResponseDTO> toResponseDTOList(List<Movimentacao> movimentacoes);
}
