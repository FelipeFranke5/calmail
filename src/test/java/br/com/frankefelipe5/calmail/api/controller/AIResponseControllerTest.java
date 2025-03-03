package br.com.frankefelipe5.calmail.api.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.service.AIResponseService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class AIResponseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AIResponseService aiResponseService;

  @BeforeEach
  void setUp(WebApplicationContext webContext, RestDocumentationContextProvider restDocumentation) {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(webContext)
            .apply(documentationConfiguration(restDocumentation).operationPreprocessors().withRequestDefaults(modifyHeaders().remove("Host")).withResponseDefaults(prettyPrint()))
            .build();
  }

  @Test
  @DisplayName("getResponseById return HTTP 200 (OK) when AIResponse is found")
  void getResponseById_ReturnsOkWithAIResponse() throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    AIResponseDTO responseDTO =
        new AIResponseDTO(
            id, "response text", new AIRequest("example", false, false, "example"), now);
    Mockito.when(aiResponseService.findResponseById(id)).thenReturn(responseDTO);
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/hal+json"))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.responseText").value("response text"))
        .andDo(document("responses/generated/get-response-by-id", pathParameters(
          parameterWithName("id").description("ID da resposta. Formato: UUID.")),
          responseFields(
            subsectionWithPath("request").description("Informações da requisição."),
            fieldWithPath("id").description("ID da resposta. Formato: UUID."),
            fieldWithPath("responseText").description("A resposta da IA. Formato: String."),
            fieldWithPath("request.name").description("Nome do solicitante enviado na requisição. Formato: String."),
            fieldWithPath("request.recurrent").description("Se na requisição foi especificado que é um problema recorrente. Formato: Boolean."),
            fieldWithPath("request.hasProtocol").description("Se na requisição foi especificado que foi gerado um protocolo. Formato: Boolean."),
            fieldWithPath("request.protocolDeadlineInDays").description("Prazo para o protocolo ser resolvido. Formato: Integer."),
            fieldWithPath("request.protocolStatus").description("Status do protocolo. Formato: String."),
            fieldWithPath("request.message").description("Mensagem enviada na requisição. Formato: String."),
            fieldWithPath("createdAt").description("Data de criação da resposta. Formato da data: AAAA-MM-DDTHH:MM:SS.MS.")
          )
        ));
  }

  @Test
  @Disabled
  void getResponseById_ReturnsNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(aiResponseService.findResponseById(id))
        .thenThrow(new AIResponseNotFoundException("unable to find an response with id = " + id));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  @Disabled
  void getResponseById_ReturnsBadRequest() throws Exception {
    String invalidId = "invalid-uuid";
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", invalidId))
        .andExpect(status().isBadRequest());
  }
}
