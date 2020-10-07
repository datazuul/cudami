package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface CollectionService extends NodeService<Collection>, EntityService<Collection> {

  boolean addChild(Collection parent, Collection child);

  boolean addChildren(Collection parent, List<Collection> children);

  boolean addDigitalObject(Collection collection, DigitalObject digitalObject);

  boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects);

  List<Collection> getActiveChildren(UUID uuid);

  PageResponse<Collection> getActiveChildren(UUID uuid, PageRequest pageRequest);

  PageResponse<DigitalObject> getDigitalObjects(Collection collection, PageRequest pageRequest);

  List<Collection> getParents(UUID uuid);

  PageResponse<Collection> getTopCollections(PageRequest pageRequest);

  boolean removeChild(Collection parent, Collection child);

  boolean removeDigitalObject(Collection collection, DigitalObject digitalObject);

  /**
   * Removes a digitalObject from all collections, to which it was connected to.
   *
   * @param digitalObject the digital object
   * @return boolean value for success
   */
  boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject);

  boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects);

  Collection saveWithParentCollection(Collection collection, UUID parentUuid)
      throws IdentifiableServiceException;
}
