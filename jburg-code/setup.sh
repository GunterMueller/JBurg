# Suppress ant 1.8 warning; this feature is
# not in ant 1.7 so it can't go in build.xml
export ANT_OPTS=-Dbuild.sysclasspath=ignore
