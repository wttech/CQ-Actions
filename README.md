![Cognifide logo](http://cognifide.com/~/media/wireframe/int/images/cognifide_logo.png)

# CQ Actions

## Purpose

CQ Actions is a mechanism serving as the underlying transport layer, which ensures that data is properly and safely transported from publish instances to author instance and is processed on the latter. 

## Features

* Seamless communication from CQ publish to author
* Messages contain key-value map, where key is a `String` and value is a `String`, `Calendar` or a primitive (`int`, `float`, etc.)
* You may register any number of author services listening to a given message topic

## Prerequisites

* CQ 5.6.1 or AEM 6

## Installation

Add dependencies to your project:

    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId>com.cognifide.cq.actions.api</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId>com.cognifide.cq.actions.core</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId><!-- choose appropriate transport type, see below --></artifactId>
        <version>3.0.0</version>
    </dependency>

## Usage

Implement data processing using `com.cognifide.actions.api.ActionReceiver` interface. Remember to create OSGi descriptors for the class:

    @Service
    @Component
    public class MyActionReceiver implements ActionReceiver {
    
        private static final Logger LOGGER = LoggerFactory.getLogger(MyActionReceiver.class);
    
        @Override
        public String getType() {
            return "my-action";
        }
    
        @Override
        public void handleAction(ValueMap properties) {
            LOGGER.info("received action: " + properties);
        }
    
    }

On the publish instance, whenever you would like to invoke any action on author instance just invoke following snippet:

    @Reference
    private ActionSubmitter actionSubmitter;
    
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("company name", "Cognifide");
    properties.put("city", "Poznan");
    properties.put("awesome", true);
    actionSubmitter.sendAction("my-action", properties);

Once, the `sendAction()` is invoked, the action will be send to the author instance and appropriate `ActionReceiver` will be called.

## Transport types

CQ Actions supports a few types of the transport layer to get the message delivered from publish to author.

### Reverse-replication

This is the classic approach, where messages are serialized into pages and the standard reverse-replication mechanism transfers them to the author. The author instance connects to the publish every 30 seconds to check if there is some user-generated content to reverse-replicated, so it may take a while before the `ActionReceiver` gets its message.

* compatible with: CQ 5.6.1 and AEM 6.
* bundle to use: `com.cognifide.cq.actions.msg.replication`.

### Push

The author instance `GET`s the publish `/bin/cognifide/cq-actions` servlet. The servlet doesn't drop the connection, but holds it and puts each serialized action as a response fragments. Author confirms receiving action with a separate `POST`. The messages are delivers immediately.

* compatible with: CQ 5.6.1 and AEM 6.
* bundle to use: `com.cognifide.cq.actions.msg.push`.

### Websockets

The author connects to the publish using websocket. Created connection is used to deliver messages, which can be received immediately.

* compatible with: AEM 6.
* bundle to use: `com.cognifide.cq.actions.msg.websocket`.

# Commercial Support

Technical support can be made available if needed. Please [contact us](http://www.cognifide.com/contact/) for more details.

We can:

* prioritize your feature request,
* tailor the product to your needs,
* provide a training for your engineers,
* support your development teams.
