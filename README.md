# About CQ-Actions

## Purpose

CQ Actions is a mechanism serving as the underlying transport layer, which ensures that data is properly and safely transported from publishers instances to author instance and is processed on the second one. 

## Prerequisites

## Installation

Add dependency to your project:

<dependency>
    <groupId>com.cognifide.cq.actions</groupId>
    <artifactId>cq-actions</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>

## Usage

Implement data processing using `com.cognifide.actions.api.ActionReceiver` interface. Remember to create OSGi descriptors for the class:

    @Service
    @Component
    public class MyAction implements ActionReceiver {
        private final static Logger LOGGER = LoggerFactory.getLogger(MyAction.class);

        @Override
        public String accepts(String actionType) {
            return "my-action".equals(actionType);
        }

        @Override
        public void handleAction(Map<String, String> properties) throws Exception {
            LOGGER.info("performing action");
        }
    }

On publish instance, whenever you would like to invoke any action on author instance just invoke following snippet:

    @Reference
    ActionSubmitter actionSubmitter;
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("some-action-key", "this is a value");
    actionSubmitter.sendAction("my-action", properties);

Once, the `sendAction()` is invoked, the action would be reverse-replicated to the author instance and one of the `EventHandler`s (`ActionPageListener`) will intercept the node creation event and fire proper action.

### Setup Jobs Queue for CQ-Actions
Setup jobs queue adds ability to to adjust jobs queue type (eg. *Ordered*), number of job retries if action job fails, etc.

To do so:

1. On Author instance go to to Adobe CQ5 Web Console Configuration | Felix Console (http://localhost:4502/system/console/configMgr)
2. Find "Apache Sling Job Queue Configuration" and add new entry.
3. Set "Topics" field to "com/cognifide/actions/defaultActionsTopic".
4. Setup other fields according to your the needs and save

* Please note that if jobs queue won't be setup (or will be setup incorectly) CQ-Actions will use default main jobs-queue which is parallel (in certain circumstances it may be unwanted).

#### CQ-Action jobs queue tests

For testing purpose  check two files from /src/test/java folder :

* SimpleActionExample.java
* CreateActionNodes.groovy 

`SimpleActionExample` must be running on Author instance. (you can simply move it to src/java, build and install created JAR as bundle).
`CreateActionNodes` is Groovy script witch creates special nodes under /content/usergenerated/action/ path in JCR (same as ActionRegistryService.createActionNode method).
Adjust parameters in script, run it and check the CQ logs.

# Commercial Support

Technical support can be made available if needed. Please [contact us](http://www.cognifide.com/contact/) for more details.

We can:

* prioritize your feature request,
* tailor the product to your needs,
* provide a training for your engineers,
* support your development teams.