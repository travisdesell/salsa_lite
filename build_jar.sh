./parse_and_build.sh

VERSION=0.0.2

echo "Generating jar file..."
jar cf ./SalsaLite-$VERSION.jar `find common` `find compiler` `find runtime`
