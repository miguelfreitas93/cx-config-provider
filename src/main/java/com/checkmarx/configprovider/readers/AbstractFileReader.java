package com.checkmarx.configprovider.readers;

import com.checkmarx.configprovider.dto.ResourceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Getter
public abstract class AbstractFileReader extends Parsable {

    protected ResourceType type;
    protected Config config;

    protected AbstractFileReader(){}

    protected static boolean isYml(String name) {
        return ResourceType.YAML == ResourceType.getTypeByNameOrExtention(name);
    }

    
    Config jsonToConfig(String fileContent) {
        return ConfigFactory.parseString(fileContent);
    }

    Config yamlToConfig(String yamlContent, String path) throws ConfigurationException {
        try{

            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());

            Object obj = yamlReader.readValue(yamlContent, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            String jsonAsStr = jsonWriter.writeValueAsString(obj)
                /*
                 *  replace a single " with space if it is not escaped (\")
                 *  a value surrounded by " or a value with no " will be resolved. ex. "java home is ${JAVA_HOME}"
                 *  a value surrounded by \" will not be resolved. ex. \"this is a string with special characters $ { \"
                 */
                .replaceAll("(?<!\\\\)\"{1}"," ")
                .replace("\\\\\\\"","\"");
            return ConfigFactory.parseString(jsonAsStr);

        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Unable to parse YAML configuration file " + path);
        }
    }

    Config jsonToConfig(URL file) {
        return ConfigFactory.parseURL(file);
    }
    
    Config yamlToConfig(URL url)  {
        throw new UnsupportedOperationException();
    }

    Config yamlToConfig(File file) throws ConfigurationException {

        try {
            String yamlContent = IOUtils.toString(new FileInputStream(file.getPath()), StandardCharsets.UTF_8);

            return yamlToConfig(yamlContent, file.getPath()) ;

        } catch (IOException e) {
            throw new ConfigurationException("Unable to read URL " + file.getPath());

        }

        
    }

    Config jsonToConfig(File file) throws ConfigurationException {

        try {
            String jsonContent = IOUtils.toString(new FileInputStream(file.getPath()), StandardCharsets.UTF_8);

            return jsonToConfig(jsonContent) ;

        } catch (IOException e) {
            throw new ConfigurationException("Unable to read URL " + file.getPath());

        }

        
    }
}
