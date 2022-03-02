package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.config.CudamiConfigClient;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.model.exception.TechnicalException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
  private final CudamiConfigClient service;

  public ConfigController(CudamiClient cudamiClient) {
    this.service = cudamiClient.forConfig();
  }

  @GetMapping("/api/config")
  public CudamiConfig getConfig() throws TechnicalException {
    return service.getConfig();
  }
}