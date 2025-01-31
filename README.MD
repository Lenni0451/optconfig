# optconfig
A java library for parsing yaml config files with support for comments and default values.

- [optconfig](#optconfig)
  - [Features](#features)
  - [Usage](#usage)
    - [Loading a config](#loading-a-config)
    - [Config provider](#config-provider)
    - [Settings](#settings)
    - [Type serializers](#type-serializers)
    - [Migrators](#migrators)
    - [Config class](#config-class)

## Features
- Loading and saving of config files from various sources (File, Path, Memory)
- Sections (including nested sections)
- Default comments and config header
- Default values for options
- Keeping of user comments and formatting (as long as the config is not broken)
- Config versioning and migration
- Automatically fixing broken configs (adding missing options, removing unknown options, resetting options with invalid values)
- Custom type serializers
- Validators for options
- Option dependencies for validators and type serializers

## Usage
### Loading a config
First you need to create a `ConfigLoader` to load your config from a file. You need to pass the class of the config object you want to load to the constructor.
```java
ConfigLoader<TestConfig> loader = new ConfigLoader<>(TestConfig.class);
```
---
After that you can load the config from a file.
```java
ConfigContext<TestConfig> configContext = loader.load(ConfigProvider.path(FileSystems.getDefault().getPath("test.yml")));
```
The returned `ConfigContext` object contains the instance of the loaded config (if the config is not static) and provides a `reload` and `save` method.
```java
configContext.reload();
configContext.save();
```

### Config provider
The `ConfigProvider` class gives you the ability to choose where the config is loaded from and where it is saved to. This also allows for fully in-memory configs.\
Some default providers can be created using the static methods of the `ConfigProvider` class.
```java
ConfigProvider#file(final File file);
ConfigProvider#path(final Path path);
ConfigProvider#memory(final String content, final Consumer<String> contentConsumer);
ConfigProvider#memory(final Supplier<String> contentSupplier, final Consumer<String> contentConsumer);
```

### Settings
The `ConfigLoader` has some settings that can be changed to modify the behavior of the loader.\
Here are some example options, but there are more available.
```java
loader.getConfigOptions().setResetInvalidOptions(true);   //Reset options with invalid values
loader.getConfigOptions().setSpaceBetweenOptions(true);   //Add a newline between options
loader.getConfigOptions().setRewriteConfig(true);         //Rewrite the config file on load (resetting user comments and formatting)
loader.getConfigOptions().setNotReloadableComment(false); //Add a comment to options that are not reloadable
...
```
Check out the `ConfigOptions` class for all available options. All options are documented using javadoc.

### Type serializers
When wanting to use custom types in your config, you need to create a type serializer for that specific type.\
Here is an example of a type serializer for the `Month` enum:
```java
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

import java.time.Month;
import java.util.Locale;

public class TestMonthSerializer extends ConfigTypeSerializer<TestConfig, Month> {

    //The required default constructor taking the config instance as parameter
    public TestMonthSerializer(final TestConfig config) {
        super(config);
    }

    //Deserialize the object from a value in the config
    //The type may vary depending on the type of the option
    @Override
    public Month deserialize(Class<Month> typeClass, Object serializedObject) {
        //this.config is the instance of the config class
        //You can access other options using it. Setting dependencies is recommended to ensure the correct order of loading.

        //Make sure to handle invalid values
        //If the type is not usable, throw an exception (e.g. InvalidSerializedObjectException)
        return Month.valueOf(((String) serializedObject).toUpperCase(Locale.ROOT));
    }

    @Override
    public Object serialize(Month object) {
        //Serialize the object to a yaml compatible object
        return object.name().toLowerCase(Locale.ROOT);
    }

}
```

### Migrators
The `OptConfig` annotation takes a config version (default is 1) which can be used to migrate the config from one version to another.\
Registering migrators is not required, but recommended when the config structure changes. When no migrator is found for a version, the changed values will be reset to their default values.\
Here is an example of a migrator:
```java
import net.lenni0451.optconfig.migrate.ConfigMigrator;

import java.util.Map;

public class TestMigrator implements ConfigMigrator {

    @Override
    public void migrate(int currentVersion, Map<String, Object> loadedValues) {
        loadedValues.remove("TestOption2"); //Removed old option

        loadedValues.put("TestOption3", "Test String 3"); //Added new option

        Object value = loadedValues.remove("TestOption");
        loadedValues.put("TestOption4", value); //Renamed option
    }

}
```
The migrator takes the current version of the config and a map of all loaded values.\
The map contains a name-value pair for each option in the config.\
Section are in the map as a nested map with the section name as key.\
When the migrator is done, the map is used to update the config object.

### Config class
Here is an example of a config class using all features of the library:
```java
import net.lenni0451.optconfig.ConfigContext;
import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.migrate.ConfigMigrator;

import java.time.Month;

@OptConfig(header = {
        "-----------------------------------",
        "| This is a great example config! |",
        "-----------------------------------"
}) //The header will be appended to the top of the config file
@Migrator(from = 1, to = 2, migrator = ConfigMigrator.class) //Migrator for version 1 to 2
@Migrator(from = 2, to = 3, migrator = ConfigMigrator.class) //Migrator for version 2 to 3
@CheckSuperclasses //Check superclasses for config options (only useful if extending another config class)
public class TestConfig {

    public ConfigContext<TestConfig> context; //This field is automatically set by the ConfigLoader

    //Reloadable by default
    @Option("TestOption") //The name of the option in the config
    @Description({"This is a test option", "It is used for testing"}) //The description of the option
    public String test = "Test String";

    @NotReloadable //Not reloadable
    @Option("TestOption2")
    @Description({"This is a test option", "It is used for testing"})
    public String test2 = "Test String 2";

    @Option //No name specified -> Field name is used
    //@NotReloadable //Sections can also be marked as not reloadable. This will also affect all options in this section
    @Description("This is a test section") //Sections can have descriptions
    public TestSection section; //Sections are automatically instantiated. Instantiating them manually will also work


    @Section //Sections need to be annotated with @Section
    public static class TestSection {
        @Option(dependencies = "test") //This option depends on "test" being loaded first. The value of "test" can be accessed in the validator or type serializers
        @Description("Enum option")
        @TypeSerializer(TestMonthSerializer.class) //Custom type serializer only for this option
        @Order(Integer.MAX_VALUE) //The position of the option in the serialized config
        public Month month = Month.JUNE;

        @Option //No name specified -> Field name is used
        //No description specified -> No comments are added
        @Hidden //Hidden options are only saved to the config if they have a value other than the default value
        public String test = "Test String 3";

        @Validator("test") //Validate the value of the option "test"
        private String validate(String test) { //Validators need to take and return the same type as the option
            //The returned value is used instead of the value in the config
            if (test.length() > 20) return "String is too long";
            else return test;
        }
    }

}
```
The serialized config will look like this:
```yaml
#-----------------------------------
#| This is a great example config! |
#-----------------------------------

#This is a test option
#It is used for testing
TestOption: Test String

#This is a test option
#It is used for testing
TestOption2: Test String 2

#This is a test section
section:
  #Enum option
  month: june
  
  test: Test String 3
```

## Including in your project
Check out [maven central](https://mvnrepository.com/artifact/net.lenni0451/optconfig) for the latest version.
### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.lenni0451:optconfig:x.x.x"
}
```

### Maven
```xml
<dependency>
  <groupId>net.lenni0451</groupId>
  <artifactId>optconfig</artifactId>
  <version>x.x.x</version>
</dependency>
```
