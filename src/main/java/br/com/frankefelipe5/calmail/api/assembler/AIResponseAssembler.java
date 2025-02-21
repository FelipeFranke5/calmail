package br.com.frankefelipe5.calmail.api.assembler;

import br.com.frankefelipe5.calmail.api.controller.AIResponseController;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AIResponseAssembler implements RepresentationModelAssembler<AIResponse, AIResponseDTO> {

    @SuppressWarnings("null")
    @Override
    public AIResponseDTO toModel(AIResponse entity) {
        AIResponseDTO responseDTO = new AIResponseDTO(entity.getId(), entity.getResponseText());
        responseDTO.add(linkTo(methodOn(AIResponseController.class).getResponseById(entity.getId())).withSelfRel());
        responseDTO.add(linkTo(methodOn(AIResponseController.class).listResponses()).withRel("list"));
        responseDTO.add(linkTo(methodOn(AIResponseController.class).clear()).withRel("clear"));
        return responseDTO;
    }

}
