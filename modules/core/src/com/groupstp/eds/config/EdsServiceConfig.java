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
    @Default("te-13eecc9c-0f4d-4bba-9410-7d40e1149fe8")
    String getContainerAlias();

    @Property("eds.sign.container.password")
    @Default("12345")
    String getContainerPassword();
}
