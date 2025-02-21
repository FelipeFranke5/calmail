package br.com.frankefelipe5.calmail.api.repository;

import br.com.frankefelipe5.calmail.api.model.AIResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIResponseRepository extends JpaRepository<AIResponse, UUID> {

    List<AIResponse> findByOrderByCreatedAtAsc();

}
