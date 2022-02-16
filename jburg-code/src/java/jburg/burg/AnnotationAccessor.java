package jburg.burg;

/**
 * An AnnotationAccessor describes the get/set
 * sequences used by an i-node to manage its
 * JBurg annotation.  Note that if not annotation
 * management sequences are specified, JBurg will
 * manage the annotations.
 */
public class AnnotationAccessor
{
    public final String getParameterName;
    public final String getAccessor;
    public final String setINodeName;
    public final String setParameterName;
    public final String setParameterType;
    public final String setAccessor;

    public AnnotationAccessor(
        String getParameterName,
        String getAccessor,
        String setINodeName,
        String setParameterType,
        String setParameterName,
        String setAccessor
    )
    {
        this.getParameterName = getParameterName;
        this.getAccessor = getAccessor;
        this.setINodeName = setINodeName;
        this.setParameterType = setParameterType;
        this.setParameterName = setParameterName;
        this.setAccessor = setAccessor;
    }

    public String toString()
    {
        return String.format(
            "AnnotationAccessor(%s,%s: %s\n%s: %s\n)",
            getParameterName,
            getAccessor,
            setINodeName,
            setAccessor
        );

    }
}
