# Upgrading the Component Registry REST service

## Procedure for updating

- When upgrading to a new version of the ComponentRegistry, check if there are
  updates to the schema of the PostgreSQL database. These will be named 
  upgrade-x.yy.sql, e.g. upgrade-1.10.sql for version 1.10.

- If the database needs upgrading, first make a backup (dump) of the existing
  database. Stop the application before doing so

- Execute the upgrade script. When upgrading over multiple minor versions, 
  execute the upgrade scripts incrementally

- Make sure to grant all required rights (GRANT ALL unless specified
  otherwise) to the component registry database user (usually compreg) on
  any newly added tables, views, and sequences.

- Then redeploy the WAR and start the application

## Version specific update instructions

### 2.5.0

- The following new context parameter(s) need to be (re)configured:

  <!-- Base location of the Concept Registry service -->
  <Parameter name="eu.clarin.cmdi.componentregistry.ccrRestUrl" value="{{DOCKER_COMPREG_CCR_REST_SERVICE}}"/>
  <!--  Base location of the CLAVAS vocabulary service  -->
  <Parameter name="eu.clarin.cmdi.componentregistry.clavasRestUrl" value="{{DOCKER_COMPREG_CLAVAS_REST_SERVICE}}"/>
  
  <!-- Skosmos cache refresh interval in seconds -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.cacheRefreshRateSeconds" value="{{DOCKER_COMPREG_SKOSMOS_CACHE_REFRESH_RATE}}"/>
  
  <!-- CCR filters -->
  <!--    * Included Skosmos vocabularies for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.ccr.includedVocabularies" value="{{DOCKER_COMPREG_SKOSMOS_CCR_INCLUDED_VOCABS}}" />
  <!--    * Included Skosmos schemes for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.ccr.includedSchemes" value="{{DOCKER_COMPREG_SKOSMOS_CCR_INCLUDED_SCHEMES}}" />

  <!-- CLAVAS filters -->
  <!--    * Included Skosmos vocabularies for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.vocabs.includedVocabularies" value="{{DOCKER_COMPREG_SKOSMOS_VOCABS_INCLUDED_VOCABS}}" />
  <!--    * Excluded Skosmos vocabularies for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.vocabs.excludedVocabularies" value="{{DOCKER_COMPREG_SKOSMOS_VOCABS_EXCLUDED_VOCABS}}" />
  <!--    * Included Skosmos schemes for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.vocabs.includedSchemes" value="{{DOCKER_COMPREG_SKOSMOS_VOCABS_INCLUDED_SCHEMES}}" />
  <!--    * Excluded Skosmos schemes for concepts -->
  <Parameter name="eu.clarin.cmdi.componentregistry.skosmos.vocabs.excludedSchemes" value="{{DOCKER_COMPREG_SKOSMOS_VOCABS_EXCLUDED_SCHEMES}}" />

### 2.4.0
- A database schema update is required. The script for this update can be found as
`upgrade-2.4.sql`
- The front end used with this REST service instance must be upgraded to version 2.4.0

### 2.3.1
Maintenance release, no special actions required if upgrading from 2.3.0

### 2.3.0
- Java 11 or higher is required

- The front end needs to be deployed separately. See 
[component-registry-front-end](https://github.com/clarin-eric/component-registry-front-end).

- An update to the database schema is required. Carry this out after stopping
the previous version but before starting the new version when deploying. Use
the script 'upgrade-2.3.sql' bundled with the deployment package.

For this release, it is strongly required to use a Docker based deployment strategy.
See [compose_compreg](https://gitlab.com/CLARIN-ERIC/compose_compreg) and the
[CLARIN Trac wiki pages](https://trac.clarin.eu/wiki/ComponentRegistryAndEditor).

### 2.2
- An update to the database schema is required. Carry this out after stopping
the previous version but before starting the new version when deploying. Use
the script 'upgrade-2.2.sql' bundled with the deployment package.

- The following new context parameter(s) need to be configured:

    <Parameter 
        name="eu.clarin.cmdi.componentregistry.clavasRestUrl" 
        value="https://openskos.meertens.knaw.nl/clavas/api/"/>
        
    Note: until the production release of OpenSKOS 2 (December 2016?), use the following 
    URL as value (but never for production purposes!):

    	http://145.100.58.79/clavas/public/api/
    	
Note: Version 2.2.1 is a maintenance update and no actions are required when upgrading
from 2.2.0 to 2.2.1.

### 2.1

- This is the first version to support CMDI 1.2. Therefore all components and
  profiles in the database need to be converted to CMDI 1.2 once before running
  the application. Use the provided 'Component spec updater' utility for this.
  It has its own bundled usage instructions.

- The following new context parameters need to be configured:

    <Parameter 
      name="eu.clarin.cmdi.componentregistry.toolkitLocation" 
      value="https://infra.clarin.eu/CMDI/1.x"/>

    <Parameter 
      name="eu.clarin.cmdi.componentregistry.component2SchemaXslUrl.cmdi_1_1"
      value="https://infra.clarin.eu/CMDI/1.1/xslt/comp2schema-v2/comp2schema.xsl"/>


- The following context parameters are no longer used and can be removed:

    * eu.clarin.cmdi.componentregistry.componentSpecSchemaLocation

 - The following parameters need to be updated to link to right CMDI toolkit 
   locations:

    <Parameter 
        name="eu.clarin.cmdi.componentregistry.generalComponentSchemaUrl" 
        value="https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd"/>

    <Parameter 
        name="eu.clarin.cmdi.componentregistry.component2SchemaXslUrl"
        value="https://infra.clarin.eu/CMDI/1.x/xslt/comp2schema.xsl"/>

    <Parameter 
        name="eu.clarin.cmdi.componentregistry.component2SchemaXslUrl.cmdi_1_1" 
        value="https://infra.clarin.eu/CMDI/1.1/xslt/comp2schema-v2/comp2schema.xsl"/>

### 2.0

- The application's public base URL is configured differently as of this 
  release. 

  The following context parameter is NO LONGER USED:

    eu.clarin.cmdi.componentregistry.serviceRootUrl <- DEPRECATED

  Instead, configure the parameter:

    eu.clarin.cmdi.componentregistry.serviceUrlPath
        e.g. "/ds/ComponentRegistry" (or "" for root deployment)
  and

    EITHER
        eu.clarin.cmdi.componentregistry.serviceUrlBase
            e.g. "http://catalog.clarin.eu"
    OR (if the application is running behind a proxy)
            eu.clarin.cmdi.componentregistry.serviceUrlProtocolHeader
                    e.g. (usually) "X-FORWARDED-PROTO"
                AND
            eu.clarin.cmdi.componentregistry.serviceUrlHostHeader
                    e.g. (usually) "X-FORWARDED-HOST"

  See the bundled context.xml for details and examples.

- The scheme for bookmark URLs of the front end have changed. Add a rewrite rule
  that causes a redirect from
  
    http://server/ComponentRegistry?param1=x&param2=y
                                   ^
  to
  
    http://server/ComponentRegistry#/?param1=x&param2=y
                                   ^^^
  (Difference marked)

- Since the JS front end (like the previous Flash based front end) is quite heavy
  on the loading of compressible content, consider enabling content compression
  on the Tomcat server to improve performance, especially on clients with slow
  connections. In particular the loading of content of types 'application/json'
  and 'application/javascript' benefits from this. 
  
  E.g., use the following in the Tomcat's server.xml:

    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               compression="on" 
               compressableMimeType="text/html,text/xml,text/plain,text/javascript,text/css,application/json,application/javascript"
               />

  More information can be found at the following page:
  	<https://tomcat.apache.org/tomcat-7.0-doc/config/http.html>

- Remove the context parameter eu.clarin.cmdi.componentregistry.jpaDialect from the global
  context.xml or application context fragment, as it is now provided within the web.xml 
  of the application itself.
  
### 1.14.5

The context parameter eu.clarin.cmdi.componentregistry.isocatRestUrl has been replaced
with the new parameter eu.clarin.cmdi.componentregistry.ccrRestUrl. An example 
configuration with the default value for the new parameter is:


  <Parameter 
  		name="eu.clarin.cmdi.componentregistry.ccrRestUrl" 
  		value="https://openskos.meertens.knaw.nl/ccr/api/"/>

### 1.14.2

No additional steps required.

### 1.14.1

No additional steps required.

### 1.14.0

- Execute DB update script upgrade-1.14.sql

- Add the following context parameter (default values given):

  <Parameter 
	  name="eu.clarin.cmdi.componentregistry.jpaDialect" 
	  value="org.hibernate.dialect.PostgreSQLDialect"/> 
	  
- ComponentRegistry no longer provides its own JDBC driver jar. In case the Tomcat 
  container does NOT provide a suitable PostgreSQL JDBC driver yet:

    - Download a suitable version and deploy it as a provided library in the Tomcat


### 1.13

- The following context fragment have been renamed, and should be updated
  in the context fragment for the Component Registry:
  
  componentRegistryServiceRootUrl -> eu.clarin.cmdi.componentregistry.serviceRootUrl
  componentRegistryAdminUsers -> eu.clarin.cmdi.componentregistry.adminUsers
  
  The results should look something like:
  <Parameter 
  		name="eu.clarin.cmdi.componentregistry.serviceRootUrl" 
  		value="http://catalog.clarin.eu/ds/ComponentRegistry" /> 
  <Parameter 
  		name="eu.clarin.cmdi.componentregistry.adminUsers" 
  		value="admin1@clarin.eu admin2@clarin.eu" />

- The following should be added to the context fragment for the Component
  Registry:

	<Parameter
			name="eu.clarin.cmdi.componentregistry.documentationUrl" 
			value="http://www.clarin.eu/cmdi" /> 
	<Parameter 
			name="eu.clarin.cmdi.componentregistry.generalComponentSchemaUrl" 
			value="https://infra.clarin.eu/cmd/general-component-schema.xsd" /> 
	<Parameter 
			name="eu.clarin.cmdi.componentregistry.component2SchemaXslUrl" 
			value="https://infra.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl" /> 
	<Parameter 
			name="eu.clarin.cmdi.componentregistry.componentSpecSchemaLocation" 
			value="http://www.clarin.eu/cmd https://infra.clarin.eu/cmd/general-component-schema.xsd" /> 
	<Parameter 
			name="eu.clarin.cmdi.componentregistry.isocatRestUrl" 
			value="https://catalog.clarin.eu/isocat/rest/" /> 
			
### 1.11 ==

- The following should be added to the context fragment for the Component
  Registry:
    <Parameter
	name="componentRegistryAdminUsers"
	value="space-separated-list-of-admin-eppns"/>

- Added to database schema:
	* comments
	* comments_id_seq
