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
    - [CLI arguments](#cli-arguments)
  - [Including in your project](#including-in-your-project)
    - [Gradle](#gradle)
    - [Maven](#maven)

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
- Parsing CLI arguments to update the config values

## Usage
### Loading a config
First you need to create a `ConfigLoader` to load your config from a file. You need to pass the class of the config object you want to load to the constructor.
```java
ConfigLoader<TestConfig> loader = new ConfigLoader<>(TestConfig.class);
```
---
After that you can load the config from a file.
```java
ConfigContext<TestConfig> configContext = loader.load(ConfigProvider.file(new File("test.yml")));
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
ConfigProvider#readOnlyFile(final File file);
ConfigProvider#path(final Path path);
ConfigProvider#readOnlyPath(final Path path);
ConfigProvider#memory(final byte[] content, final Consumer<byte[]> contentConsumer);
ConfigProvider#memory(final Supplier<byte[]> contentSupplier, final Consumer<byte[]> contentConsumer);
ConfigProvider#empty();
```

### Settings
The `ConfigLoader` has some settings that can be changed to modify the behavior of the loader.\
Here are some example options, but there are more available.
```java
loader.getConfigOptions().setResetInvalidOptions(true);               //Reset options with invalid values
loader.getConfigOptions().setSpaceBetweenOptions(true);               //Add a newline between options
loader.getConfigOptions().setRewriteConfig(true);                     //Rewrite the config file on load (resetting user comments and formatting)
loader.getConfigOptions().setNotReloadableComment("Not reloadable");  //Change or disable (using null / empty) the comment for not reloadable options
...
```
Check out the `ConfigOptions` class for all available options. All options are documented using javadoc.

### Type serializers
When wanting to use custom types in your config, you need to create a type serializer for that specific type.\
Here is an example of a type serializer for the `Month` enum:
```java
import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;

import java.time.Month;
import java.util.Locale;

public class TestMonthSerializer implements ConfigTypeSerializer<Month> {

  //Deserialize the object from a value in the config
  //The type may vary depending on the type of the option
  @Override
  public Month deserialize(final DeserializerInfo<Month> info) {
    //Make sure to handle invalid values
    if (!(info.value() instanceof String)) {
      //If the type is not usable, throw an exception (e.g. InvalidSerializedObjectException)
      throw new InvalidSerializedObjectException(String.class, info.value().getClass());
    }
    return Month.valueOf(((String) info.value()).toUpperCase(Locale.ROOT));
  }

  @Override
  public Object serialize(final SerializerInfo<Month> info) {
    //Serialize the object to a yaml compatible object
    return info.value().name().toLowerCase(Locale.ROOT);
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
Sections are in the map as a nested map with the section name as key.\
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
@CheckSuperclass //Check superclasses for config options (only useful if extending another config class)
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
    @Description(value = "This is a test section", generator = "generateDescription") //Sections can have descriptions, part of it is generated
    public TestSection section; //Sections are automatically instantiated. Instantiating them manually will also work

    private static String[] generateDescription() {
        return new String[]{"The description has been generated", "The current time is: " + System.currentTimeMillis(), "It will update every start if RewriteConfig is enabled"};
    }


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

### CLI arguments
The `cli` module allows you to parse command-line arguments to automatically update config values and format help tables.
```java
//Create a CLIConfigLoader from your ConfigContext
CLIConfigLoader<TestConfig> cliLoader = new CLIConfigLoader<>(configContext);

//Print generated CLI help to System.out
cliLoader.printCLIHelp();

//Or parse arguments to update config values
cliLoader.loadCLIOptions(args);
```
Options can be configured with CLI-specific annotations:
- `@CLIName`: Define a custom CLI argument name and optionally set `omitSection = true` to flatten nested section names.
- `@CLIAliases`: Add one or more alias names (e.g. short flags like `{"-h", "--help"}`) and optionally hide them from help output with `hidden = true`.
- `@CLIRequired`: Mark an option as mandatory on the command line (throws `CLIMissingOptionException` if missing).
- `@CLIIgnore`: Exclude an option from being exposed or modified via the CLI.

## Including in your project
Check out [maven central](https://mvnrepository.com/artifact/net.lenni0451/optconfig) for the latest version.
### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.lenni0451.optconfig:core:x.x.x"
    implementation "net.lenni0451.optconfig:cli:x.x.x" //Optional module for parsing CLI arguments to update the config values
}
```

### Maven
```xml
<dependency>
  <groupId>net.lenni0451.optconfig</groupId>
  <artifactId>core</artifactId>
  <version>x.x.x</version>
</dependency>
```
