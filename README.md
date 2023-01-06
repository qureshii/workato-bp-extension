
OPA extension SDK allows customers to execute commands in the operating system


## Configuration

Extension mechanism is enabled by adding the `extensions` section to the config file, `conf/config.yml`:

```yml
    server:
      classpath: C:\\Program Files\\Workato Agent\\ext
    extensions:
      commandLine:
        controllerClass: com.pg.opa.controller.BluePrismController
        scriptpath: C:\Users\xyx\scripts
        scriptname: hello.bat
```

Individual endpoints are defined inside `extensions`, where every key serves as _endpoint name_. Every _endpoint name_ has to be valid identifier, has to be URL-friendly and cannot contain any special characters (like whitespace, etc.).

Classpath defines a set of folders/JAR files containing compiled extensions.


## Using extension endpoints

Every endpoint is hosted inside the REST-compliant URL: `/ext/<endpointName>/<resource>` where `<endpointName>` corresponds to key defined in the configuration file and `<resource>` is an specific REST resource within the endpoint.

Endpoint request handling logic is defined by Spring REST controller class, provided as `controllerClass` property. This controller class is registered inside an endpoint-specific Spring `WebApplicationContext` and allows retrieving configuration properties from the `config.yml` file.

## Endpoint controller sample

Full code can be found [here](https://github.com/qureshii/workato-bp-extension/blob/main/opa_extension/src/main/java/com/pg/opa/controller/BluePrismController.java).

```java
// import rest of the packages here:
@GetMapping(path = "/ping")
public ResponseEntity<ResponsePayload> ping() {
        boolean fileExist = false;
        try {

        String path = scriptpath + File.separator + scriptname;
        File file = new File(path);
        fileExist = file.exists();
        logger.info("Script {} , exist? {} ", path, fileExist);
        } catch (Exception e) {
        logger.error("Error in ping command {} ", e.getMessage());
        return errorResponse(e.getMessage(), BAD_REQUEST);
        }
        return ResponseEntity
        .status(fileExist ? OK : BAD_REQUEST)
        .body(new ResponsePayload(true, 0, null, null));
        }

```

## Building extension

Steps to build an extension:

1. Install the latest Java 8 SDK
2. Use `./gradlew jar` command to bootstrap Gradle and build the project.
3. The output is in `build/libs`.

## Installing the extension to OPA

1. Add a new directory called `ext` under Workato agent install directory.
2. Copy the extension JAR file to `ext` directory.
3. Update the `config/config.yml` to add the `ext` file to class path.

```yml
    server:
      classpath: C:\\Program Files\\Workato Agent\\ext
```

4. Update the `config/config.yml` to configure the new extension.

```yml
    extensions:
      commandLine:
        controllerClass: com.pg.opa.controller.BluePrismController
        scriptpath: C:\Users\xyx\scripts
        scriptname: hello.bat
```


## Using the extension in Recipe

In order to use the extension in a recipe, we need a custom adapter in Workato. Sample adapter for the extension 
can be found [here](https://github.com/qureshii/workato-bp-extension/blob/main/custom_sdk/bp_sdk.rb).

```ruby
    generic_process_action: {
      title: 'Generic Action',
           input_fields: -> ( obj_def) {
          obj_def['request_payload']
       },
 

      execute: ->(connection, input,object_definations) {
        post("http://localhost/ext/#{connection['profile']}/api/v1/process-actions", input['payload']).headers('X-Workato-Connector': 'enforce')
      },
      output_fields: lambda do |object_definition|
        object_definition['response_payload'] 
    
      end
    },
```
## Download Jar
A prebuild jar file can be found [here](https://github.com/qureshii/workato-bp-extension/tree/main/jar)