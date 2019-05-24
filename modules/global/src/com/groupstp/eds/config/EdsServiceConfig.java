package com.groupstp.eds.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

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
}
