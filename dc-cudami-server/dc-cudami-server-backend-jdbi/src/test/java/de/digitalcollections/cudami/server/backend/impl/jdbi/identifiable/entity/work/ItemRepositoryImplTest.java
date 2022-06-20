package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Gender;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {ItemRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The Item Repository Test")
public class ItemRepositoryImplTest {

  private ItemRepositoryImpl repo;

  @Autowired CorporateBodyRepository corporateBodyRepository;
  @Autowired AgentRepository agentRepository;
  @Autowired PersonRepository personRepository;

  @BeforeEach
  void setup(
      @Autowired Jdbi jdbi,
      @Autowired DigitalObjectRepositoryImpl digitalObjectRepository,
      @Autowired WorkRepositoryImpl workRepository,
      @Autowired CudamiConfig config) {
    repo = new ItemRepositoryImpl(jdbi, digitalObjectRepository, workRepository, config);
  }

  @Test
  void saveAndRetrieveOneHolder() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    corporateBodyRepository.save((CorporateBody) holders.get(0));

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    Item storedItem = repo.save(item);
    Item retrievedItem = repo.getByUuid(storedItem.getUuid());
    assertThat(storedItem).isEqualTo(retrievedItem);
  }

  @Test
  void saveAndRetrieveTwoHolders() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "Some Amazing Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    corporateBodyRepository.save((CorporateBody) holders.get(0));
    corporateBodyRepository.save((CorporateBody) holders.get(1));

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    Item storedItem = repo.save(item);
    Item retrievedItem = repo.getByUuid(storedItem.getUuid());
    assertThat(storedItem).isEqualTo(retrievedItem);
  }

  @Test
  @DisplayName("returns holder(s) as agents only with UUID and label and no other fields")
  void returnHoldersAsAgents() {
    CorporateBody holder1 =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label("ACME Inc.")
                .identifier("foobar", "42")
                .homepageUrl("https://www.digitale-sammlungen.de/")
                .build());
    Person holder2 =
        personRepository.save(
            Person.builder()
                .label("Karl Ranseier")
                .identifier("gnd", "-1")
                .gender(Gender.MALE)
                .description(Locale.GERMAN, "Der erfolgloseste Entwickler aller Zeiten")
                .build());

    Item item = Item.builder().label("Test-Item").holders(List.of(holder1, holder2)).build();

    Item persisted = repo.getByUuid(repo.save(item).getUuid());

    assertThat(persisted.getHolders()).hasSize(2);

    Agent itemPersistedAgent1 = persisted.getHolders().get(0);
    assertThat(itemPersistedAgent1.getUuid()).isNotNull();
    assertThat(itemPersistedAgent1.getLabel()).isNotNull();
    assertThat(itemPersistedAgent1.getIdentifiers()).isEmpty();

    Agent itemPersistedAgent2 = persisted.getHolders().get(1);
    assertThat(itemPersistedAgent2.getUuid()).isNotNull();
    assertThat(itemPersistedAgent2.getLabel()).isNotNull();
    assertThat(itemPersistedAgent2.getIdentifiers()).isEmpty();

    CorporateBody agent1 = (CorporateBody) agentRepository.getByUuid(itemPersistedAgent1.getUuid());
    assertThat(agent1).isEqualTo(holder1);

    Person agent2 = (Person) agentRepository.getByUuid(itemPersistedAgent2.getUuid());
    assertThat(agent2).isEqualTo(holder2);
  }
}
