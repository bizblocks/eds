package com.groupstp.eds.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.type.Factory;
import com.haulmont.cuba.core.config.type.IntegerListTypeFactory;
import com.haulmont.cuba.core.config.type.UuidTypeFactory;

import java.util.List;
import java.util.UUID;

@Source(type = SourceType.DATABASE)
public interface EdsServiceConfig extends Config {

    @Property("eds.sign.keystore.password")
    String getKeyStorePassword();

    @Property("eds.sign.container.alias")
    String getContainerAlias();

    @Property("eds.sign.container.password")
    @Default("00000000")
    String getContainerPassword();

    @Property("eds.sign.location")
    @Default("")
    String getLocation();

    @Property("eds.sign.contact")
    @Default("")
    String getContact();

    @Property("eds.sign.reason")
    @Default("")
    String getReason();

    @Property("eds.sign.signDocs")
    @Default("false")
    Boolean getSignDocs();

    @Property("eds.sign.appearance.coordinates")
    @Default("50 750 220 820")
    @Factory(factory = IntegerListTypeFactory.class)
    List<Integer> getAppearanceRectangleCoordinates();

    @Property("eds.sign.appearance.isPlacedInLastPage")
    @Default("false")
    boolean isPlacedInLastPage();

    @Property("eds.sign.appearance.isUseImage")
    @Default("false")
    boolean isUseImage();

    @Factory(factory = UuidTypeFactory.class)
    @Property("eds.sign.appearance.imageId")
    UUID getImageId();
}
