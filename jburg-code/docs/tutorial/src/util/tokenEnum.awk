BEGIN {
print "package " packageName ";";
print "public abstract class " className "{";
}
!/'.*'/ {print "  public static final int " $0 ";";}
END {print "}"}
