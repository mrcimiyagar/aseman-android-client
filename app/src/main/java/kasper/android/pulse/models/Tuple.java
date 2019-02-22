package kasper.android.pulse.models;

public class Tuple<A, B, C, D, E> {

    public final A first;
    public final B second;
    public final C third;
    public final D fourth;
    public final E fifth;

    public Tuple(A first, B second, C third, D fourth, E fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }
}
