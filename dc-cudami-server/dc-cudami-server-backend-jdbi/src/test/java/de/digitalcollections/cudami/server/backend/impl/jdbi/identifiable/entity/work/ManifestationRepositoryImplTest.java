package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.ProductionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.manifestation.Title;
import de.digitalcollections.model.identifiable.entity.manifestation.TitleType;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Text;
import de.digitalcollections.model.time.LocalDateRange;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = ManifestationRepositoryImpl.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("The Manifestation Repository")
class ManifestationRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<ManifestationRepositoryImpl> {

  @Autowired CorporateBodyRepository corporateBodyRepository;
  @Autowired HumanSettlementRepository humanSettlementRepository;
  @Autowired PredicateRepository predicateRepository;
  @Autowired EntityRelationRepository entityRelationRepository;
  @Autowired SubjectRepository subjectRepository;
  @Autowired ExpressionTypeMapper expressionTypeMapper;
  @Autowired LocalDateRangeMapper localDateRangeMapper;
  @Autowired TitleMapper titleMapper;
  @Autowired EntityRepositoryImpl<Entity> entityRepository;

  @BeforeEach
  void beforeEach() {
    repo =
        new ManifestationRepositoryImpl(
            jdbi,
            cudamiConfig,
            expressionTypeMapper,
            localDateRangeMapper,
            titleMapper,
            entityRepository);
  }

  @Test
  @DisplayName("1.0. Save a manifestation with parent")
  void testSaveManifestation() throws RepositoryException {
    // agents for relations
    CorporateBody editor = CorporateBody.builder().label("Editor").addName("Editor").build();
    corporateBodyRepository.save(editor);
    CorporateBody someoneElse =
        CorporateBody.builder().label("Someone else").addName("Someone else").build();
    corporateBodyRepository.save(someoneElse);

    // predicates
    Predicate isEditorOf =
        predicateRepository.save(Predicate.builder().value("is_editor_of").build());
    Predicate isSomethingElseOf =
        predicateRepository.save(Predicate.builder().value("is_somethingelse_of").build());

    // subjects
    Subject subject = ensurePersistedSubject();

    // parent
    Manifestation parent = ensurePersistedParentManifestation();

    List<Title> titles = prepareTitles();

    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    manifestation.addRelation(new EntityRelation(editor, "is_editor_of", manifestation));
    manifestation.addRelation(
        EntityRelation.builder()
            .subject(someoneElse)
            .predicate("is_somethingelse_of")
            .object(manifestation)
            .additionalPredicate("additional predicate")
            .build());
    repo.save(manifestation);

    // we add the relations manually, actually done by the service
    entityRelationRepository.save(manifestation.getRelations());

    Manifestation actual = repo.getByUuid(manifestation.getUuid());

    assertThat(actual.getTitles()).isEqualTo(titles);
    assertThat(actual.getExpressionTypes()).isEqualTo(manifestation.getExpressionTypes());

    assertThat(actual.getRelations()).size().isEqualTo(2);
    assertThat(actual.getRelations().get(0))
        .isEqualTo(new EntityRelation(editor, "is_editor_of", null));
    assertThat(actual.getRelations().get(1))
        .isEqualTo(
            EntityRelation.builder()
                .subject(someoneElse)
                .predicate("is_somethingelse_of")
                .additionalPredicate("additional predicate")
                .build());

    assertThat(actual.getSubjects()).containsExactlyInAnyOrder(subject);
    assertThat(actual.getParents())
        .containsExactlyInAnyOrder(
            new RelationSpecification<Manifestation>("The child's title", null, parent));
    assertThat(manifestation.getProductionInfo().getPublishers())
        .allSatisfy(publisher -> assertThat(publisher.getAgent().getUuid()).isNotNull());
    assertThat(manifestation.getPublicationInfo().getPublishers())
        .allSatisfy(publisher -> assertThat(publisher.getAgent().getUuid()).isNotNull());
    assertThat(actual.getProductionInfo()).isEqualTo(manifestation.getProductionInfo());
    assertThat(actual.getPublicationInfo()).isEqualTo(manifestation.getPublicationInfo());
  }

  @Test
  @DisplayName("1.1. Update a manifestation")
  void testUpdateManifestation() throws RepositoryException {
    Subject subject = ensurePersistedSubject();
    Manifestation parent = ensurePersistedParentManifestation();
    List<Title> titles = prepareTitles();
    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    repo.save(manifestation);

    // get the Manifestation saved in 1.0.
    Manifestation persisted = repo.getByUuid(manifestation.getUuid());
    manifestation.getLabel().put(Locale.ENGLISH, "An updated label");
    manifestation
        .getTitles()
        .add(
            Title.builder()
                .text(new LocalizedText(Locale.ENGLISH, "An updated Title"))
                .titleType(new TitleType("MAIN", "MAIN"))
                .build());
    manifestation.setParents(null);
    repo.update(manifestation);

    var actual = repo.getByUuid(manifestation.getUuid());
    assertThat(actual.getLabel()).isEqualTo(manifestation.getLabel());
    assertThat(actual.getTitles()).size().isEqualTo(4);
    assertThat(actual.getTitles()).isEqualTo(manifestation.getTitles());
    assertThat(actual.getParents()).isNull();
  }

  @Test
  @DisplayName("2.0. Find any manifestation")
  void testFind() throws RepositoryException {
    Subject subject = ensurePersistedSubject();
    Manifestation parent = ensurePersistedParentManifestation();
    List<Title> titles = prepareTitles();
    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    repo.save(manifestation);

    PageResponse<Manifestation> actual = repo.find(new PageRequest(0, 10));
    assertThat(actual.getTotalElements()).isEqualTo(2);
    assertThat(actual.getContent()).size().isEqualTo(2);
    assertThat(actual.getContent().stream().map(m -> m.getUuid()).toList())
        .contains(manifestation.getUuid(), parent.getUuid());
  }

  // -------------------------------------------------------------------
  private Manifestation ensurePersistedParentManifestation() throws RepositoryException {
    var noteText = new StructuredContent();
    noteText.addContentBlock(new Text("some notes"));
    var note = new LocalizedStructuredContent();
    note.put(Locale.ENGLISH, noteText);
    var manifestation =
        Manifestation.builder()
            .label(new LocalizedText(Locale.ENGLISH, "A parent manifestation"))
            .manifestationType("SERIAL")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.ENGLISH, "A parent manifestation"))
                    .build())
            .title(
                Title.builder()
                    .titleType(new TitleType("sub", "sub"))
                    .text(new LocalizedText(Locale.ENGLISH, "...and its subtitle"))
                    .build())
            .note(note)
            .build();
    repo.save(manifestation);
    return manifestation;
  }

  private Manifestation prepareManifestation(
      Subject subject, Manifestation parent, List<Title> titles) {
    Manifestation manifestation =
        Manifestation.builder()
            .label(Locale.GERMAN, "ein Label")
            .composition("composition")
            .expressionType(ExpressionType.builder().mainType("BOOK").subType("PRINT").build())
            .language(Locale.GERMAN)
            .mediaType("BOOK")
            .titles(titles)
            .subject(subject)
            .parent(new RelationSpecification<Manifestation>("The child's title", null, parent))
            .publicationInfo(
                PublicationInfo.builder()
                    .publisher(
                        Publisher.builder()
                            .agent(
                                Agent.builder()
                                    .uuid(UUID.randomUUID())
                                    .name(new LocalizedText(Locale.ENGLISH, "Publisher"))
                                    .build())
                            .build())
                    .navDateRange(
                        new LocalDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)))
                    .build())
            .productionInfo(
                ProductionInfo.builder()
                    .publisher(
                        Publisher.builder()
                            .agent(
                                Agent.builder()
                                    .uuid(UUID.randomUUID())
                                    .name(new LocalizedText(Locale.ENGLISH, "Producer"))
                                    .build())
                            .build())
                    .navDateRange(
                        new LocalDateRange(LocalDate.of(2019, 10, 1), LocalDate.of(2020, 6, 30)))
                    .build())
            .build();
    return manifestation;
  }

  private Subject ensurePersistedSubject() throws RepositoryException {
    Subject subject =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My subject"))
            .identifier(Identifier.builder().namespace("test").id("12345").build())
            .type("SUBJECT_TYPE")
            .build();
    subjectRepository.save(subject);
    return subject;
  }

  private List<Title> prepareTitles() {
    List<Title> titles =
        new ArrayList<>(
            List.of(
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Ein deutscher Titel"))
                    .titleType(new TitleType("main", "main"))
                    .textLocaleOfOriginalScript(Locale.GERMAN)
                    .textLocaleOfOriginalScript(Locale.ENGLISH)
                    .build(),
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Untertitel"))
                    .titleType(new TitleType("main", "sub"))
                    .build(),
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(
                        new LocalizedText(
                            Locale.forLanguageTag("und-Latn"),
                            "Illustrierter Sonntag : das Blatt des gesunden Menschenverstandes. 1929 ## 31.03.1929"))
                    .textLocalesOfOriginalScripts(Collections.emptySet())
                    .build()));
    return titles;
  }
}
