package euphoria.psycho.comic.util;

/**
 * Created by Administrator on 2015/1/14.
 */
public final class ObjectUtilities {

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static int hashCode(Object o) {
        return (o == null) ? 0 : o.hashCode();
    }
}
