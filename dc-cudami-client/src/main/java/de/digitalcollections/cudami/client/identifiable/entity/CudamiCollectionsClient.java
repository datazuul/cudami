package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiCollectionsClient extends CudamiEntitiesClient<Collection> {

  public CudamiCollectionsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Collection.class, mapper, "/v5/collections");
  }

  public boolean addDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid), digitalObjects));
  }

  public boolean addSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public boolean addSubcollections(UUID collectionUuid, List<Collection> subcollections)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(baseEndpoint + "/%s/subcollections", collectionUuid), subcollections));
  }

  public PageResponse<Collection> findActive(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "?active=true"), pageRequest);
  }

  public SearchPageResponse<Collection> findActive(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/search?active", searchPageRequest);
  }

  public PageResponse<Collection> findActiveSubcollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections?active=true", uuid), searchPageRequest);
  }

  public PageResponse<Collection> findActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections?active=true", uuid),
        pageRequest,
        Collection.class);
  }

  public SearchPageResponse<DigitalObject> findDigitalObjects(
      UUID collectionUuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
        searchPageRequest,
        DigitalObject.class);
  }

  public List<CorporateBody> findRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws TechnicalException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format(baseEndpoint + "/%s/related/corporatebodies", uuid),
        CorporateBody.class,
        filtering);
  }

  public PageResponse<Collection> findSubcollections(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections", uuid), searchPageRequest);
  }

  public PageResponse<Collection> findSubcollections(UUID collectionUuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections", collectionUuid),
        pageRequest,
        Collection.class);
  }

  public SearchPageResponse<Collection> findTopCollections(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/top", searchPageRequest);
  }

  public Collection getActiveByUuid(UUID uuid, Locale locale) throws TechnicalException {
    return doGetRequestForObject(
        String.format(baseEndpoint + "/%s?active=true&pLocale=%s", uuid, locale));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format(baseEndpoint + "/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  public List<Locale> getLanguagesOfTopCollections() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/top/languages", Locale.class);
  }

  public Collection getParent(UUID uuid) throws TechnicalException {
    return (Collection)
        doGetRequestForObject(String.format(baseEndpoint + "/%s/parent", uuid), Collection.class);
  }

  public List<Collection> getParents(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format(baseEndpoint + "/%s/parents", uuid));
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean removeSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws TechnicalException {
    return doPostRequestForObject(
        String.format(baseEndpoint + "/%s/collection", parentCollectionUuid), collection);
  }

  public boolean setDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
                digitalObjects,
                String.class));
  }
}
