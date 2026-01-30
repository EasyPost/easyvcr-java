## Builds the project for development
build:
    mvn clean install -DskipTests=true -Dgpg.skip=true -Dcheckstyle.skip=true -Dcheckstyle.skip=true -Ddependency-check.skip=true -Djavadoc.skip=true

## Cleans the project
clean:
    mvn clean

## Test the project and generate a coverage report
coverage:
    mvn --batch-mode install -Dgpg.skip=true -Dcheckstyle.skip=true -Dcheckstyle.skip=true -Ddependency-check.skip=true -Djavadoc.skip=true jacoco:report

## Install CheckStyle
install-checkstyle:
    curl -LJs https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.3.1/checkstyle-10.3.1-all.jar -o checkstyle.jar
    curl -LJs https://raw.githubusercontent.com/EasyPost/examples/refs/heads/master/style_guides/java/easypost_java_style.xml -o easypost_java_style.xml

## Install requirements
install: install-checkstyle
    git submodule init
    git submodule update

## Check if project follows CheckStyle rules (must run install-checkstyle first)
lint:
    java -jar checkstyle.jar src -c easypost_java_style.xml -d

## Publish a release of the project
# @parameters:
# pass= - The GPG password to sign the release
publish pass:
    mvn clean deploy -Dgpg.passphrase={{pass}}

## Build the project as a dry run to publishing
# @parameters:
# pass= - The GPG password to sign the release
publish-dry pass:
    mvn clean install -Dgpg.passphrase={{pass}}

## Cuts a release for the project on GitHub (requires GitHub CLI)
# tag = The associated tag title of the release
release tag:
    gh release create {{tag}} target/*.jar target/*.asc target/*.pom

## Test the project
test:
    mvn surefire:test
