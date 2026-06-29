package com.cleanroommc.kirino.schemata.ds;

import com.google.common.base.Preconditions;
import com.google.common.graph.PredecessorsFunction;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public final class FibonacciHeap<T extends Comparable<T>> extends AbstractQueue<T> {

    @Nullable
    private Node<T> minNode = null;
    private int length = 0;

    @Override
    @NonNull
    public Iterator<T> iterator() {
        if (minNode == null) {
            return Collections.emptyIterator();
        }
        return new HeapIterator(minNode);
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public boolean offer(T t) {
        Node<T> node = new Node<>(t);
        if (minNode == null) {
            minNode = node;
        } else {
            addToRootList(node);
            if (t.compareTo(minNode.key) < 0) {
                minNode = node;
            }
        }
        length++;
        return true;
    }

    private void addToRootList(Node<T> node) {
        Preconditions.checkNotNull(node);
        Preconditions.checkNotNull(minNode);

        node.left = minNode;
        node.right = minNode.right;
        minNode.right.left = node;
        minNode.right = node;
    }

    @Override
    @Nullable
    public T poll() {
        Node<T> min = minNode;
        if (min != null) {
            if (min.child != null) {
                addChildrenToRootList(min.child);
            }
            removeNodeFromRootList(min);
            if (min.equals(min.right)) {
                minNode = null;
            } else {
                minNode = min.right;

            }
            length--;
        }
        return min != null ? min.key : null;
    }

    private void addChildrenToRootList(@NonNull Node<T> min) {
        Preconditions.checkNotNull(min);
        Preconditions.checkNotNull(minNode);

        Node<T> child = min.child;
        do {
            Node<T> next = child.right;
            child.left = minNode;
            child.right = minNode.right;
            minNode.right.left = child;
            minNode.right = child;
            child.parent = null;
            child = next;
        } while (!child.equals(min.child));
    }

    private void removeNodeFromRootList(@NonNull Node<T> node) {
        Preconditions.checkNotNull(node);

        node.left.right = node.right;
        node.right.left = node.left;
    }

    private void consolidate() {
        final double LN2 = 0.6931471805599453094172321214581765680755001343602552541206800094;
        int arraySize = ((int)Math.floor(Math.log(length) / LN2)) + 1;
        Node<T>[] array = new Node[arraySize];
        List<Node<T>> rootList = getRootList();

        for (Node<T> node : rootList) {
            int degree = node.degree;
            while (array[degree] != null) {
                Node<T> other = array[degree];
                if (node.key.compareTo(other.key) > 0) {
                    Node<T> tmp = node;
                    node = other;
                    other = tmp;
                }

                link(other, node);
                array[degree] = null;
                degree++;
            }
            array[degree] = node;
        }

        minNode = null;
        for (Node<T> node : array) {
            if (node != null) {
                if (minNode == null) {
                    minNode = node;
                } else {
                    addToRootList(node);
                    if (node.key.compareTo(minNode.key) < 0) {
                        minNode = node;
                    }
                }
            }
        }
    }

    @NonNull
    private List<Node<T>> getRootList() {
        List<Node<T>> rootList = new ReferenceArrayList<>();
        if (minNode != null) {
            Node<T> current = minNode;
            do {
                rootList.add(current);
                current = current.right;
            } while (current != minNode);
        }
        return rootList;
    }

    private void link(@NonNull Node<T> y, @NonNull Node<T> x) {
        Preconditions.checkNotNull(y);
        Preconditions.checkNotNull(x);

        removeNodeFromRootList(y);
        y.left = y.right = y;
        y.parent = x;

        if (x.child == null) {
            x.child = y;
        } else {
            y.right = x.child;
            y.left = x.child.left;
            x.child.left.right = y;
            x.child.left = y;
        }
        x.degree++;
        y.mark = false;
    }

    @Override
    @Nullable
    public T peek() {
        if (minNode == null) {
            return null;
        }
        return minNode.key;
    }

    @Override
    public boolean remove(Object o) {
        if (minNode == null) {
            return false;
        }
        return super.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        if (minNode == null) {
            return false;
        }
        for(Node<T> node : Traverser.forTree(minNode).breadthFirst(minNode)) {
            if (node.key.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return minNode == null;
    }

    @Override
    public boolean add(T t) {
        return this.offer(t);
    }

    private static class Node<T extends Comparable<T>> implements Comparable<Node<T>>, PredecessorsFunction<Node<T>>, SuccessorsFunction<Node<T>> {
        T key;
        int degree;
        Node<T> parent, left, child, right;
        boolean mark;

        public Node(T key) {
            this.key = key;
            degree = 0;
            parent = null;
            left = this;
            child = null;
            right = this;
            mark = false;
        }

        @Override
        @NonNull
        public Iterable<? extends Node<T>> predecessors(@NonNull Node<T> node) {
            Set<Node<T>> set = new ReferenceArraySet<>();
            if (parent != null) {
                set.add(parent);
            }
            return set;
        }

        @Override
        @NonNull
        public Iterable<? extends Node<T>> successors(@NonNull Node<T> node) {
            Set<Node<T>> set = new ReferenceArraySet<>();
            if (left != null) {
                set.add(left);
            }
            if (child != null) {
                set.add(child);
            }
            if (right != null) {
                set.add(right);
            }
            return set;
        }

        @Override
        public int compareTo(@NonNull Node<T> o) {
            return key.compareTo(o.key);
        }
    }

    private class HeapIterator implements Iterator<T> {
        private Node<T> node;

        private HeapIterator(Node<T> node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node.left != null || node.child != null || node.right != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Optional<Node<T>> min = Stream.of(node.left, node.child, node.right)
                    .min(HeapIterator::compareNullables);
            node = min.get();
            return min.get().key;
        }

        // Because we can't decrease keys we need to use this abomination instead
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private static <T extends Comparable<T>> int compareNullables(@Nullable Node<T> lhs,
                                                                      @Nullable Node<T> rhs) {
            return lhs != null ? (rhs != null ? lhs.compareTo(rhs) : -1) : Integer.MAX_VALUE;
        }
    }
}
