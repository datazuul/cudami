package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
public abstract class AbstractRepositoryImplTest {

  @Autowired protected PostgreSQLContainer postgreSQLContainer;
  @Autowired protected Jdbi jdbi;
  @Autowired protected CudamiConfig cudamiConfig;
  @Autowired private DigitalCollectionsObjectMapper mapper;

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  protected <O> O createDeepCopy(O object) {
    try {
      String serializedObject = mapper.writeValueAsString(object);
      O copy = (O) mapper.readValue(serializedObject, object.getClass());

      assertThat(copy).isEqualTo(object);
      return copy;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot serialize/deserialize " + object + ": " + e, e);
    }
  }
}
