package euphoria.psycho.comic.util;


/**
 * Created by Administrator on 2015/1/13.
 */
public class Triple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <A, B, C> Triple<A, B, C> create(A a, B b, C c) {
        return new Triple<A, B, C>(a, b, c);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triple)) {
            return false;
        }
        Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
        return ObjectUtilities.equal(t.first, first) && ObjectUtilities.equal(t.second, second)&& ObjectUtilities.equal(t.third,third);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }
}
