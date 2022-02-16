int x;
x = 4;
int y;
y = 5;

if ( x == y )
    print "FAIL: x and y are equal\n";
else if ( x < y )
    print "Correct: x is less than y\n";
else if ( y < x )
    print "FAIL: y is less than x\n";

int countdown;
countdown = 10;
while ( 0 < countdown )
{
    print countdown;
    print "... ";
    countdown = countdown - 1;
}
print "done!\n";

if ( x == y )
{
    print "FAIL: ", x, " and ", y, " are equal?\n" ;
}
else if ( x < y )
{
    print "resetting \"y\"...\n";
    y = 4;
    if ( x == y )
        print "now ", x, " == ", y, ", good\n";
    else
        print "FAIL: expected y to be 4, actual ", y, "\n";
}
else
{
    print "FAIL: x is greater than y\n";
}

