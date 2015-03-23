cd $1
mvn clean install sling:install
mvn sling:install -Dinstance.url=http://localhost:4503
