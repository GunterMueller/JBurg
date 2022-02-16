set +x
V1GRAMMAR=$1
V1BASENAME=`basename $V1GRAMMAR`
V2GRAMMAR=$V1GRAMMAR.v2

SCRIPTDIR=`dirname $0`

# Do the conversion.
sed -f $SCRIPTDIR/v1ToV2.sed $V1GRAMMAR > $V2GRAMMAR
mv $V2GRAMMAR $V1GRAMMAR

# Now run any tests that reference this grammar.
XMLFILES=`find . -name \*.xml | xargs grep $V1BASENAME | cut -d : -f 1,1`

if [ "$XMLFILES" != "" ]
then
    for file in $XMLFILES
    do
        if [ $file != "./build.xml" ]
        then
            echo Testing: $file
            ant -Dburm.test.file=$file test
        else
            echo Manual conversion required: $V1BASENAME appears in build.xml:
            grep -C 3 $V1BASENAME build.xml
        fi
    done
else
    echo $V1BASENAME does not appear in any test.xml file.
fi
