package com.gigabiba.cloudfilestorage;

import com.gigabiba.cloudfilestorage.controllers.AuthControllerIntegrationTest;
import com.gigabiba.cloudfilestorage.controllers.StorageControllerIntegrationTest;
import com.gigabiba.cloudfilestorage.controllers.UserControllerIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthControllerIntegrationTest.class,
        UserControllerIntegrationTest.class,
        StorageControllerIntegrationTest.class
})
class TestSuite {
}
