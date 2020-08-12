package com.cx.configprovider.services;

import com.cx.configprovider.dto.ConfigLocation;
import com.cx.configprovider.dto.RawConfigAsCode;
import com.cx.configprovider.dto.RemoteRepoLocation;
import com.cx.configprovider.dto.SourceProviderType;


import com.cx.configprovider.exceptions.ConfigProviderException;
import com.cx.utility.TestPropertyLoader;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.util.Properties;

import static org.junit.Assert.*;


public class RemoteRepoConfigDownloaderSteps {
    private static final String GITHUB_REPO = "Cx-FlowRepo";
    private static final String GITHUB_NAMESPACE = "cxflowtestuser";
    private static final String BRANCH = "orly-config-provider-tests";
    private static final String GITHUB_TOKEN = "github.token";
    private static final String GITHUB_API_URL = "https://api.github.com";
    static Properties props;
    private static SourceProviderType providerType;
    private RawConfigAsCode config;
    private Exception exception;

    @Before()
    public static void loadProperties() {
        TestPropertyLoader propertyLoader = new TestPropertyLoader();
        props = propertyLoader.getProperties();
    }

    @When("repository source is GITHUB")
    public void setRepositorySource(){
        providerType = SourceProviderType.GITHUB;
        exception = null;
    }

    
    @Then ("configuration provider will retrieve the configuration {string} from repository")
    public void setPath(String path){
        try {
            config = getConfigFromPath(path);
        } catch (Exception e) {
            exception = e;
        }
    }
    @And("the the returned configuration object will be {string}")
    public void checkOutput(String expected) {
        if(exception!=null){
            return;
        }
        else if(config == null){
            fail("config not populated");
        }
        else if(expected.equals("empty")){
            assertEmptyContent(config);
        }else{
            assertNonEmptyContent(config);
        }
    }
    
    @And("exception will be {string}")
    public void verifyException(String isException){
        if(Boolean.parseBoolean(isException)){
            assertEquals("Unexpected exception type.", ConfigProviderException.class, exception.getClass());
        }
    }

    private static void assertEmptyContent(RawConfigAsCode config) {
        Assert.assertTrue("Expected Config-as-code file content to be empty.", config.getContent().isEmpty());
    }

    private static void assertNonEmptyContent(RawConfigAsCode config) {
        assertTrue("Config-as-code file content is empty.", StringUtils.isNotEmpty(config.getContent()));
    }

    private static RawConfigAsCode getConfigFromPath(String path) {

        RemoteRepoLocation repoLocation = getRemoteRepoLocation();

        ConfigLocation location = ConfigLocation.builder()
                .path(path)
                .repoLocation(repoLocation)
                .build();

        RemoteRepoConfigDownloader downloader = new RemoteRepoConfigDownloader();

        RawConfigAsCode result = downloader.getConfigAsCode(location);
        assertNotNull("Config-as-code object must always be non-null.", result);
        assertNotNull("File content must always be non-null.", result.getContent());

        return result;
    }

    private static RemoteRepoLocation getRemoteRepoLocation() {
        RemoteRepoLocation repoLocation;
        if(SourceProviderType.GITHUB.equals(providerType)) {
             repoLocation = RemoteRepoLocation.builder()
                    .apiBaseUrl(GITHUB_API_URL)
                    .repoName(GITHUB_REPO)
                    .namespace(GITHUB_NAMESPACE)
                    .ref(BRANCH)
                    .accessToken(props.getProperty(GITHUB_TOKEN))
                    .sourceProviderType(SourceProviderType.GITHUB)
                    .build();
        }else{
            throw new UnsupportedOperationException();
        }
        return repoLocation;
    }
}