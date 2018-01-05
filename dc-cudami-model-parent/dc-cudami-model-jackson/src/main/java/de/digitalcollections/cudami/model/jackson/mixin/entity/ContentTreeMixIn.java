package de.digitalcollections.cudami.model.jackson.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.cudami.model.impl.identifiable.entity.ContentTreeImpl;

@JsonDeserialize(as = ContentTreeImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("contentTree")
public interface ContentTreeMixIn {

}
