package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Website handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class WebsiteServiceImpl extends EntityServiceImpl<Website> implements WebsiteService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  private final WebpageService webpageService;

  @Autowired
  public WebsiteServiceImpl(
      WebsiteRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      CudamiConfig cudamiConfig,
      WebpageService webpageService) {
    super(repository, identifierRepository, urlAliasService, cudamiConfig);
    this.webpageService = webpageService;
  }

  @Override
  public SearchPageResponse<Webpage> findRootWebpages(UUID uuid, SearchPageRequest pageRequest) {
    return webpageService.findRootWebpagesForWebsite(uuid, pageRequest);
  }

  @Override
  public List<Webpage> getRootWebpages(UUID uuid) {
    return ((WebsiteRepository) repository).getRootWebpages(uuid);
  }

  @Override
  public boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootWebpagesOrder(website, rootPages);
  }

  @Override
  public boolean updateRootWebpagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootWebpagesOrder(websiteUuid, rootPages);
  }
}
