![Cognifide logo](http://cognifide.com/~/media/wireframe/int/images/cognifide_logo.png)

# CQ Actions

## Purpose

CQ Actions is a mechanism serving as the underlying transport layer, which ensures that data is properly and safely transported from publishers instances to author instance and is processed on the second one. 

## Features

* Seamless communication from CQ publish to author using reverse replication
* Messages contain key-value string map
* You may register any number of author services listening to a given message topic

## Prerequisites

* CQ 5.6.1 or AEM 6

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
    public class MyReceiver implements ActionReceiver {
        private final static Logger LOGGER = LoggerFactory.getLogger(MyAction.class);

        @Override
        public String accepts(String actionType) {
            return "my-action".equals(actionType);
        }

        @Override
        public void handleAction(Map<String, String> properties) throws Exception {
            LOGGER.info("received value: " + properties.get("company name"));
        }
    }

On publish instance, whenever you would like to invoke any action on author instance just invoke following snippet:

    @Reference
    private ActionSubmitter actionSubmitter;
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("company name", "Cognifide");
    properties.put("city", "Poznan");
    actionSubmitter.sendAction("my-action", properties);

Once, the `sendAction()` is invoked, the action would be reverse-replicated to the author instance and one of the `EventHandler`s (`ActionPageListener`) will intercept the node creation event and fire proper action.

### Setup jobs queue for CQ Actions

Read on [wiki](https://github.com/Cognifide/CQ-Actions/wiki/Setup-Jobs-Queue-for-CQ-Actions).

# Commercial Support

Technical support can be made available if needed. Please [contact us](http://www.cognifide.com/contact/) for more details.

We can:

* prioritize your feature request,
* tailor the product to your needs,
* provide a training for your engineers,
* support your development teams.