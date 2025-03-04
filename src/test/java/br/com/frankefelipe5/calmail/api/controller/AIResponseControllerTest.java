package br.com.frankefelipe5.calmail.api.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.frankefelipe5.calmail.api.assembler.AIResponseAssembler;
import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.exception.AuthResponseException;
import br.com.frankefelipe5.calmail.api.exception.ErrorCode;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.service.AIResponseService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
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
            .apply(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(modifyHeaders().remove("Host").remove("Vary"))
                    .withResponseDefaults(prettyPrint()))
            .build();
  }

  @Test
  @DisplayName("getResponseById returns HTTP 200 (OK) when AIResponse is found")
  void getResponseById_ReturnsOk_WithAIResponse() throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    AIResponse aiResponse =
        new AIResponse(id, "response text", new AIRequest("example", false, false, "example"), now);
    AIResponseAssembler aiResponseAssembler = new AIResponseAssembler();
    AIResponseDTO responseDTO = aiResponseAssembler.toModel(aiResponse);
    Mockito.when(aiResponseService.findResponseById(id)).thenReturn(responseDTO);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/responses/generated/{id}", id)
                .header("Authorization", "Bearer tokenExample"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/hal+json"))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.responseText").value("response text"))
        .andExpect(jsonPath("$._links").exists())
        .andDo(
            document(
                "responses/generated/get-response-by-id",
                pathParameters(
                    parameterWithName("id")
                        .description("ID da resposta. Formato: UUID. Obrigatório: Sim.")),
                responseFields(
                    subsectionWithPath("request")
                        .description(
                            "Nó que representa as informações enviadas na requisição. Formato: Object."),
                    subsectionWithPath("_links")
                        .description(
                            "Nó que representa os links disponíveis para consulta. Formato: Object."),
                    fieldWithPath("id").description("ID da resposta. Formato: UUID."),
                    fieldWithPath("responseText").description("A resposta da IA. Formato: String."),
                    fieldWithPath("request.name")
                        .description("Nome do solicitante enviado na requisição. Formato: String."),
                    fieldWithPath("request.recurrent")
                        .description(
                            "Se na requisição foi especificado que é um problema recorrente. Formato: Boolean."),
                    fieldWithPath("request.hasProtocol")
                        .description(
                            "Se na requisição foi especificado que foi gerado um protocolo. Formato: Boolean."),
                    fieldWithPath("request.protocolDeadlineInDays")
                        .description(
                            "Prazo para o protocolo ser resolvido. Formato: Number ou Null."),
                    fieldWithPath("request.protocolStatus")
                        .description("Status do protocolo. Formato: String ou Null."),
                    fieldWithPath("request.message")
                        .description("Mensagem enviada na requisição. Formato: String."),
                    fieldWithPath("createdAt")
                        .description(
                            "Data de criação da resposta. Formato da data: AAAA-MM-DDTHH:MM:SS.MS.")),
                requestHeaders(
                    headerWithName("Authorization")
                        .description(
                            "Token de autenticação. Formato: Bearer token. Obrigatório: Sim.")),
                links(
                    HypermediaDocumentation.halLinks(),
                    linkWithRel("self").description("Link para o acesso ao recurso atual."),
                    linkWithRel("delete").description("Link para apagar o recurso."),
                    linkWithRel("list")
                        .description("Link para listar todas as respostas (Sem ordenação)."),
                    linkWithRel("list-order")
                        .description("Link para listar todas as respostas (Com ordenação)."),
                    linkWithRel("create").description("Link para criar uma nova resposta."),
                    linkWithRel("clear").description("Link para limpar as respostas."))));
  }

  @Test
  @DisplayName("getResponseById returns HTTP 404 (Not Found) when AIResponse is not found")
  void getResponseById_When_ServiceThrowsAIResponseNotFoundException_ReturnsNotFound()
      throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(aiResponseService.findResponseById(id))
        .thenThrow(new AIResponseNotFoundException("unable to find an response with id = " + id));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/responses/generated/{id}", id)
                .header("Authorization", "Bearer tokenExample"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.AI_RESPONSE_NOT_FOUND_EXCEPTION.value()))
        .andExpect(jsonPath("$.message").value("unable to find an response with id = " + id))
        .andDo(
            document(
                "responses/generated/get-response-by-id-not-found",
                pathParameters(
                    parameterWithName("id")
                        .description("ID da resposta. Formato: UUID. Obrigatório: Sim.")),
                responseFields(
                    fieldWithPath("code").description("Código do erro. Formato: Number."),
                    fieldWithPath("message").description("Mensagem do erro. Formato: String.")),
                requestHeaders(
                    headerWithName("Authorization")
                        .description(
                            "Token de autenticação. Formato: Bearer token. Obrigatório: Sim."))));
  }

  @Test
  @DisplayName("getResponseById Return HTTP 400 (BadRequest) when an invalid ID is inserted")
  void getResponseById_When_IdIsNotValid_ReturnsBadRequest() throws Exception {
    String invalidId = "invalid-uuid";
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/responses/generated/{id}", invalidId)
                .header("Authorization", "tokenExample"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.value()))
        .andExpect(jsonPath("$.message").value("Invalid UUID string: " + invalidId))
        .andDo(
            document(
                "responses/generated/get-response-by-id-with-invalid-id",
                pathParameters(
                    parameterWithName("id")
                        .description("ID da resposta. Formato: UUID. Obrigatório: Sim.")),
                responseFields(
                    fieldWithPath("code").description("Código do erro. Formato: Number."),
                    fieldWithPath("message").description("Mensagem do erro. Formato: String.")),
                requestHeaders(
                    headerWithName("Authorization")
                        .description(
                            "Token de autenticação. Formato: Bearer token. Obrigatório: Sim."))));
  }

  @Test
  @DisplayName("getResponseById returns HTTP 401 (Unauthorized) when the user is not authenticated")
  void getResponseById_When_UserIsNotAuthenticated_ReturnsUnauthorized() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(aiResponseService.findResponseById(id))
        .thenThrow(new AuthResponseException("please authenticate to proceed"));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", id))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("please authenticate to proceed"))
        .andDo(
            document(
                "responses/generated/get-response-by-id-unauthorized",
                pathParameters(
                    parameterWithName("id")
                        .description("ID da resposta. Formato: UUID. Obrigatório: Sim."))));
  }
}
