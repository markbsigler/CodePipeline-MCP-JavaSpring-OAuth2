package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
public class SimpleMessageRepositoryIT {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveMessage() {
        // Given
        Message message = new Message();
        message.setContent("Test content");
        message.setSender("test@example.com");

        // When
        Message savedMessage = messageRepository.save(message);
        Optional<Message> foundMessage = messageRepository.findById(savedMessage.getId());

        // Then
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getContent()).isEqualTo("Test content");
        assertThat(foundMessage.get().getSender()).isEqualTo("test@example.com");
    }
}
