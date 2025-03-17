package com.witboost.provisioning.dq.sifflet.config;

import static io.vavr.API.Some;
import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.model.Workload;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

/*
 * TODO Review these tests after you have implemented the tech adapter logic
 */
class ClassProviderBeanTest {

    ClassProviderBean classProviderBean = new ClassProviderBean();

    @Test
    void defaultSpecificProvider() {
        var specificProvider = classProviderBean.specificClassProvider();

        assertEquals(Option.of(SiffletSpecific.class), specificProvider.get("a-urn"));
    }

    @Test
    void defaultComponentProvider() {
        var componentProvider = classProviderBean.componentClassProvider();

        assertEquals(Some(Workload.class), componentProvider.get("whatever"));
    }
}
