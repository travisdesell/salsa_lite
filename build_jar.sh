./parse_and_build.sh

VERSION=0.0.3

echo "Generating jar file..."
cd ..
jar cf ./SalsaLite-$VERSION.jar `find salsa_lite/common` `find salsa_lite/compiler` `find salsa_lite/runtime`
