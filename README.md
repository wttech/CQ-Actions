# About CQ-Actions

## Purpose

CQ Actions is a mechanism serving as the underlying transport layer, which ensures that data is properly and safely transported from publishers instances to author instance and is processed on the second one. 

## Prerequisites

## Installation

### CQ 5.6:

Download latest stable version: https://github.com/Cognifide/CQ-Actions/archive/cq-actions-1.2.0.zip

Compile and install to your local repository:

    mvn clean install

Add dependency to your project:

    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId>cq-actions</artifactId>
        <version>1.2.0</version>
    </dependency>

Or just install it using your Felix console.

### AEM 6.0:

Download latest stable version: https://github.com/Cognifide/CQ-Actions/archive/cq-actions-2.0.0-incubator.zip

Compile and install to your local repository:

    mvn clean install

Add dependency to your project:

    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId>cq-actions</artifactId>
        <version>2.0.0-incubator</version>
    </dependency>

Or just install it using your Felix console.

## Usage

Implement data processing using `com.cognifide.actions.api.Action` interface. Remember to create OSGi descriptors for the class:

    @Service
    @Component
    public class MyAction implements Action {

        private final static Logger LOGGER = LoggerFactory.getLogger(MyAction.class);

        @Override
        public void perform(Page page) throws Exception {
            LOGGER.info("performing action");
        }

        public String getType() {
            return "my-action";
        }

    }

On publish instance, whenever you would like to invoke any action on author instance just invoke following snippet:

    @Reference
    ActionRegistry actionRegistryService;

    ...

    Node node = actionRegistryService.createActionNode(session, relPath, "my-action");
    node.setProperty(name, value);
    session.save();

Once, the `session.save()` is invoked, the node would be replicated to author instance and one of the `EventHandler`s (`ActionHandleEventListener`) will intercept the node creation event and fire proper action.

You might want to change the path on which the `ActionHandleEventListener` is listening using OSGi Configuration as well as the root path for creating action nodes in `ActionRegistryService` configuration.

### Setup Jobs Queue for CQ-Actions
Setup jobs queue adds ability to to adjust jobs queue type (eg Ordered), number of job retries if action job fails etc.

To do so:

1. On Author instance goto to Adobe CQ5 Web Console Configuration | Felix Console (http://localhost:4502/system/console/configMgr)
2. Find "Apache Sling Job Queue Configuration" and add new entry.
3. Set "Topics" field to "com/cognifide/actions/defaultActionsTopic".
4. Setup other fields according to your the needs and save

* Please note that if jobs queue wont be setup (or will be setup incorectly) CQ-Actions will use default main jobs-queue witch is parallel (this in certain circumstances it may be unwanted).

#### CQ-Action jobs queue tests

For testing purpose  check two files from /src/test/java folder :

* SimpleActionExample.java
* CreateActionNodes.groovy 

SimpleActionExample mus be running on Author instance. (you can simply move it to src/java, build and install created JAR as bundle).
CreateActionNodes is Groovy script witch creates special nodes under /content/usergenerated/action/ path in JCR (same as ActionRegistryService.createActionNode method).
Adjust parameters in script, run it and check the CQ logs.

# Commercial Support

Technical support can be made available if needed. Please [contact us](https://www.cognifide.com/get-in-touch/) for more details.

We can:

* prioritize your feature request,
* tailor the product to your needs,
* provide a training for your engineers,
* support your development teams.

More documentation
------------------
* [Cognifide.com](http://cognifide.com)
