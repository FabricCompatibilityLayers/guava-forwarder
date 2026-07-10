package io.github.fabriccompatibilitylayers.guavaforwarder.g16_0.stubs;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.*;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStub;
import io.github.fabriccompatibilitylayers.guavaforwarder.GuavaStubs;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Factories and utilities pertaining to the {@link C_Constraint} interface.
 *
 * @see MapConstraints
 * @author Mike Bostock
 * @author Jared Levy
 * @since 3.0
 */
@GuavaStubs("com/google/common/collect/Constraints")
public class C_Constraints {
    // enum singleton pattern
    private enum NotNullConstraint implements C_Constraint<Object> {
        INSTANCE;

        @Override
        public Object checkElement(Object element) {
            return checkNotNull(element);
        }

        @Override public String toString() {
            return "Not null";
        }
    }

    @GuavaStub
    public static <E> C_Constraint<E> notNull() {
        return (C_Constraint<E>) NotNullConstraint.INSTANCE;
    }

    @GuavaStub
    public static <E> Collection<E> constrainedCollection(
            Collection<E> collection, C_Constraint<? super E> constraint) {
        return new C_Constraints.ConstrainedCollection<E>(collection, constraint);
    }

    /** @see C_Constraints#constrainedCollection */
    static class ConstrainedCollection<E> extends ForwardingCollection<E> {
        private final Collection<E> delegate;
        private final C_Constraint<? super E> constraint;

        public ConstrainedCollection(
                Collection<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = checkNotNull(delegate);
            this.constraint = checkNotNull(constraint);
        }
        @Override protected Collection<E> delegate() {
            return delegate;
        }
        @Override public boolean add(E element) {
            constraint.checkElement(element);
            return delegate.add(element);
        }
        @Override public boolean addAll(Collection<? extends E> elements) {
            return delegate.addAll(checkElements(elements, constraint));
        }
    }

    /**
     * Returns a constrained view of the specified set, using the specified
     * constraint. Any operations that add new elements to the set will call the
     * provided constraint. However, this method does not verify that existing
     * elements satisfy the constraint.
     *
     * <p>The returned set is not serializable.
     *
     * @param set the set to constrain
     * @param constraint the constraint that validates added elements
     * @return a constrained view of the set
     */
    @GuavaStub
    public static <E> Set<E> constrainedSet(
            Set<E> set, C_Constraint<? super E> constraint) {
        return new C_Constraints.ConstrainedSet<E>(set, constraint);
    }

    /** @see C_Constraints#constrainedSet */
    static class ConstrainedSet<E> extends ForwardingSet<E> {
        private final Set<E> delegate;
        private final C_Constraint<? super E> constraint;

        public ConstrainedSet(Set<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = checkNotNull(delegate);
            this.constraint = checkNotNull(constraint);
        }
        @Override protected Set<E> delegate() {
            return delegate;
        }
        @Override public boolean add(E element) {
            constraint.checkElement(element);
            return delegate.add(element);
        }
        @Override public boolean addAll(Collection<? extends E> elements) {
            return delegate.addAll(checkElements(elements, constraint));
        }
    }

    /**
     * Returns a constrained view of the specified sorted set, using the specified
     * constraint. Any operations that add new elements to the sorted set will
     * call the provided constraint. However, this method does not verify that
     * existing elements satisfy the constraint.
     *
     * <p>The returned set is not serializable.
     *
     * @param sortedSet the sorted set to constrain
     * @param constraint the constraint that validates added elements
     * @return a constrained view of the sorted set
     */
    @GuavaStub
    public static <E> SortedSet<E> constrainedSortedSet(
            SortedSet<E> sortedSet, C_Constraint<? super E> constraint) {
        return new C_Constraints.ConstrainedSortedSet<E>(sortedSet, constraint);
    }

    /** @see C_Constraints#constrainedSortedSet */
    private static class ConstrainedSortedSet<E> extends ForwardingSortedSet<E> {
        final SortedSet<E> delegate;
        final C_Constraint<? super E> constraint;

        ConstrainedSortedSet(
                SortedSet<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = checkNotNull(delegate);
            this.constraint = checkNotNull(constraint);
        }
        @Override protected SortedSet<E> delegate() {
            return delegate;
        }
        @Override public SortedSet<E> headSet(E toElement) {
            return constrainedSortedSet(delegate.headSet(toElement), constraint);
        }
        @Override public SortedSet<E> subSet(E fromElement, E toElement) {
            return constrainedSortedSet(
                    delegate.subSet(fromElement, toElement), constraint);
        }
        @Override public SortedSet<E> tailSet(E fromElement) {
            return constrainedSortedSet(delegate.tailSet(fromElement), constraint);
        }
        @Override public boolean add(E element) {
            constraint.checkElement(element);
            return delegate.add(element);
        }
        @Override public boolean addAll(Collection<? extends E> elements) {
            return delegate.addAll(checkElements(elements, constraint));
        }
    }

    /**
     * Returns a constrained view of the specified list, using the specified
     * constraint. Any operations that add new elements to the list will call the
     * provided constraint. However, this method does not verify that existing
     * elements satisfy the constraint.
     *
     * <p>If {@code list} implements {@link RandomAccess}, so will the returned
     * list. The returned list is not serializable.
     *
     * @param list the list to constrain
     * @param constraint the constraint that validates added elements
     * @return a constrained view of the list
     */
    @GuavaStub
    public static <E> List<E> constrainedList(
            List<E> list, C_Constraint<? super E> constraint) {
        return (list instanceof RandomAccess)
                ? new C_Constraints.ConstrainedRandomAccessList<E>(list, constraint)
                : new C_Constraints.ConstrainedList<E>(list, constraint);
    }

    /** @see C_Constraints#constrainedList */
    @GwtCompatible
    private static class ConstrainedList<E> extends ForwardingList<E> {
        final List<E> delegate;
        final C_Constraint<? super E> constraint;

        ConstrainedList(List<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = checkNotNull(delegate);
            this.constraint = checkNotNull(constraint);
        }
        @Override protected List<E> delegate() {
            return delegate;
        }

        @Override public boolean add(E element) {
            constraint.checkElement(element);
            return delegate.add(element);
        }
        @Override public void add(int index, E element) {
            constraint.checkElement(element);
            delegate.add(index, element);
        }
        @Override public boolean addAll(Collection<? extends E> elements) {
            return delegate.addAll(checkElements(elements, constraint));
        }
        @Override public boolean addAll(int index, Collection<? extends E> elements)
        {
            return delegate.addAll(index, checkElements(elements, constraint));
        }
        @Override public ListIterator<E> listIterator() {
            return constrainedListIterator(delegate.listIterator(), constraint);
        }
        @Override public ListIterator<E> listIterator(int index) {
            return constrainedListIterator(delegate.listIterator(index), constraint);
        }
        @Override public E set(int index, E element) {
            constraint.checkElement(element);
            return delegate.set(index, element);
        }
        @Override public List<E> subList(int fromIndex, int toIndex) {
            return constrainedList(
                    delegate.subList(fromIndex, toIndex), constraint);
        }
    }

    /** @see C_Constraints#constrainedList */
    static class ConstrainedRandomAccessList<E> extends C_Constraints.ConstrainedList<E>
            implements RandomAccess {
        ConstrainedRandomAccessList(
                List<E> delegate, C_Constraint<? super E> constraint) {
            super(delegate, constraint);
        }
    }

    /**
     * Returns a constrained view of the specified list iterator, using the
     * specified constraint. Any operations that would add new elements to the
     * underlying list will be verified by the constraint.
     *
     * @param listIterator the iterator for which to return a constrained view
     * @param constraint the constraint for elements in the list
     * @return a constrained view of the specified iterator
     */
    private static <E> ListIterator<E> constrainedListIterator(
            ListIterator<E> listIterator, C_Constraint<? super E> constraint) {
        return new C_Constraints.ConstrainedListIterator<E>(listIterator, constraint);
    }

    /** @see C_Constraints#constrainedListIterator */
    static class ConstrainedListIterator<E> extends ForwardingListIterator<E> {
        private final ListIterator<E> delegate;
        private final C_Constraint<? super E> constraint;

        public ConstrainedListIterator(
                ListIterator<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = delegate;
            this.constraint = constraint;
        }
        @Override protected ListIterator<E> delegate() {
            return delegate;
        }

        @Override public void add(E element) {
            constraint.checkElement(element);
            delegate.add(element);
        }
        @Override public void set(E element) {
            constraint.checkElement(element);
            delegate.set(element);
        }
    }

    static <E> Collection<E> constrainedTypePreservingCollection(
            Collection<E> collection, C_Constraint<E> constraint) {
        if (collection instanceof SortedSet) {
            return constrainedSortedSet((SortedSet<E>) collection, constraint);
        } else if (collection instanceof Set) {
            return constrainedSet((Set<E>) collection, constraint);
        } else if (collection instanceof List) {
            return constrainedList((List<E>) collection, constraint);
        } else {
            return constrainedCollection(collection, constraint);
        }
    }

    /**
     * Returns a constrained view of the specified multiset, using the specified
     * constraint. Any operations that add new elements to the multiset will call
     * the provided constraint. However, this method does not verify that
     * existing elements satisfy the constraint.
     *
     * <p>The returned multiset is not serializable.
     *
     * @param multiset the multiset to constrain
     * @param constraint the constraint that validates added elements
     * @return a constrained view of the multiset
     */
    @GuavaStub
    public static <E> Multiset<E> constrainedMultiset(
            Multiset<E> multiset, C_Constraint<? super E> constraint) {
        return new ConstrainedMultiset<E>(multiset, constraint);
    }

    /** @see C_Constraints#constrainedMultiset */
    static class ConstrainedMultiset<E> extends ForwardingMultiset<E> {
        private Multiset<E> delegate;
        private final C_Constraint<? super E> constraint;

        public ConstrainedMultiset(
                Multiset<E> delegate, C_Constraint<? super E> constraint) {
            this.delegate = checkNotNull(delegate);
            this.constraint = checkNotNull(constraint);
        }
        @Override protected Multiset<E> delegate() {
            return delegate;
        }
        @Override public boolean add(E element) {
            return standardAdd(element);
        }
        @Override public boolean addAll(Collection<? extends E> elements) {
            return delegate.addAll(checkElements(elements, constraint));
        }
        @Override public int add(E element, int occurrences) {
            constraint.checkElement(element);
            return delegate.add(element, occurrences);
        }
        @Override public int setCount(E element, int count) {
            constraint.checkElement(element);
            return delegate.setCount(element, count);
        }
        @Override public boolean setCount(E element, int oldCount, int newCount) {
            constraint.checkElement(element);
            return delegate.setCount(element, oldCount, newCount);
        }
    }

    /*
     * TODO(kevinb): For better performance, avoid making a copy of the elements
     * by having addAll() call add() repeatedly instead.
     */

    private static <E> Collection<E> checkElements(
            Collection<E> elements, C_Constraint<? super E> constraint) {
        Collection<E> copy = Lists.newArrayList(elements);
        for (E element : copy) {
            constraint.checkElement(element);
        }
        return copy;
    }
}
