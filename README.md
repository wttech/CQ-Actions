# About CQ-Actions

## Purpose

CQ Actions is a mechanism serving as the underlying transport layer, which ensures that data is properly and safely transported from publishers instances to author instance and is processed on the second one. 

## Prerequisites

## Installation

Download latest stable version: https://github.com/Cognifide/CQ-Actions/archive/1.0.0.zip

Compile and install to your local repository:

    mvn clean package install

Add dependency to your project:

    <dependency>
        <groupId>com.cognifide.cq.actions</groupId>
        <artifactId>cq-actions</artifactId>
        <version>1.0.0</version>
    </dependency>

Or just install it using your Felix console.

## Usage

Implement data processing using `com.cognifide.actions.api.Action` interface. Remember to create OSGi descriptors for the class:

    @Service
    @Component
    public class MyAction {

        private final static LOGGER = LoggerFactory.getLogger(MyAction.class);

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
    ActionRegistryService actionRegistryService;

    ...

    Node node = actionRegistryService.createActionNode(session, relPath, "my-action");
    node.setProperty(name, value);
    session.save();

Once, the `session.save()` is invoked, the node would be replicated to author instance and one of the `EventHandler`s (`ActionHandleEventListener`) will intercept the node creation event and fire proper action.

You might want to change the path on which the `ActionHandleEventListener` is listening using OSGi Configuration as well as the root path for creating action nodes in `ActionRegistryService` configuration.

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
