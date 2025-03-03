package br.com.frankefelipe5.calmail.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import br.com.frankefelipe5.calmail.api.controller.AIResponseController;
import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AIResponseAssembler
    implements RepresentationModelAssembler<AIResponse, AIResponseDTO> {

  @SuppressWarnings("null")
  @Override
  public AIResponseDTO toModel(AIResponse entity) {
    AIRequest requestExample = new AIRequest("example", false, false, "example");
    AIResponseDTO responseDTO =
        new AIResponseDTO(
            entity.getId(), entity.getResponseText(), entity.getRequest(), entity.getCreatedAt());
    responseDTO.add(
        linkTo(methodOn(AIResponseController.class).getResponseById(entity.getId())).withSelfRel());
    responseDTO.add(
        linkTo(methodOn(AIResponseController.class).deleteResponseById(entity.getId()))
            .withRel("delete"));
    responseDTO.add(
        linkTo(methodOn(AIResponseController.class).listResponses("false")).withRel("list"));
    responseDTO.add(
        linkTo(methodOn(AIResponseController.class).listResponses("true")).withRel("list-order"));
    responseDTO.add(
        linkTo(methodOn(AIResponseController.class).createScript(requestExample))
            .withRel("create"));
    responseDTO.add(linkTo(methodOn(AIResponseController.class).clear()).withRel("clear"));
    return responseDTO;
  }
}
