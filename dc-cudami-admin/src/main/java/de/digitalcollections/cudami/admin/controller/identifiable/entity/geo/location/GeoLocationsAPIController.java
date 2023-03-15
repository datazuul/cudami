package de.digitalcollections.cudami.admin.controller.identifiable.entity.geo.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiGeoLocationsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Controller for all public "GeoLocations" endpoints (API). */
@RestController
public class GeoLocationsAPIController
    extends AbstractEntitiesController<GeoLocation, CudamiGeoLocationsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationsAPIController.class);

  public GeoLocationsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forGeoLocations(), languageService);
  }

  @GetMapping("/api/geolocations/new")
  @ResponseBody
  public GeoLocation create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/geolocations")
  @ResponseBody
  public BTResponse<GeoLocation> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(GeoLocation.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }
}
