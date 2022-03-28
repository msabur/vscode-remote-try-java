// openjml --esc $@
/** This file contains a worked (specified and verified) example of a doubly-linked list using JML and OpenJML. 
 * It is intended to illustrate techniques that can be used for other simple recursive data structures. 
 * This example uses model fields.
 * 
 * A list of values of type T is an instance of the public class ListDLL<T>. The actual links are instances of the non-public
 * nested class Node<T>; both classes inherit from Link<T>, which contains common functionality. The linked-list is formed
 * by the typical chain of 'next' and 'prev' field references, with a null reference at the end of the list. The starting point, the List
 * object, is not a part of the abstract list.
 * 
 */

/** Parent class for ListDLL and Node containing the 'next' field and common functionality */
class Link<T> {

    //@ public model JMLDataGroup ownerFields;
    //@ ghost helper nullable public ListDLL<T> owner; //@ in ownerFields; maps next.ownerFields \into ownerFields;

    //@ model public seq<T> values; // sequence of values after the current Link, not including the current Link
    //@ public represents values = next == null ? seq.<T>empty() : next.values.prepend(next.value);

    //@ model public seq<Link<T>> links;
    //@ public represents links = next == null ? seq.<Link<T>>empty() : next.links.prepend(next);

    // True if my owner is the argument and all nodes after me have the argument as their owners.
    //@ public normal_behavior
    //@   reads this.owner, this.next.ownerFields, this.links;
    //@   ensures \result == (this.owner == owner && (next != null ==> next.allMine(owner)));
    //@ model pure helper public boolean allMine(ListDLL<T> owner);

    //@ model public \bigint size; 
    //@ public represents size = ((next == null) ? 0 : 1 + next.size);
    
    //@ public invariant this.owner != null && allMine(this.owner);
    //@ public invariant this.next != null ==> this.next.prev == this;
    //@ public invariant this.prev != null ==> this.prev.next == this;
    //@ public invariant values.size() == size;
    //@ public invariant links.size() == size;
    //@ public invariant !links.contains(this);

    //@ nullable spec_public
    protected ListDLL.Node<T> next; //@ in size, values, links; 
    //@ maps next.values \into values; maps next.size \into size; maps next.links \into links;
    
    //@ nullable spec_public
    protected Link<T> prev; //@ in links; maps next.prev \into links;
    
    //@ protected normal_behavior
    //@ pure helper
    protected Link() {
    }

    //@ public normal_behavior // only for demonstration purposes -- exposes representation
    //@   reads next;
    //@   ensures \result == next;
    //@ pure helper
    //@ public model ListDLL.Node<T> next();
        
    /** Returns the nth value in the list */
    //@ public normal_behavior
    //@   requires 0 <= n < this.size;
    //@   reads values, links;
    //@   ensures \result == values[n];
    //@ pure
    public T value(int n) {
        if (n == 0) return next.value();
        else return next.value(n-1);
    }

    /** Removes the nth value from the list */
    //@ public normal_behavior
    //@   requires 0 <= n < this.size;
    //@   requires this.owner != null && allMine(this.owner); // FIXME - needed?
    //@   {|
    //@      requires this.next.next == null;
    //@      assignable size, values, links;
    //@    also
    //@      requires this.next.next != null;
    //@      assignable size, values, links, this.next.next.prev;
    //@   |}
    //@   ensures this.size == \old(this.size) - 1;
    //@   ensures n == 0 ==> next == \old(next.next);
    //@   ensures n == 0 ==> values == \old(values).tail(1);
    //@   ensures n > 0 ==> next == \old(next);
    //@   ensures this.owner != null && allMine(this.owner); // FIXME - needed?
    public void remove(int n) {
        if (n == 0) {
            //@ assert this.owner != null && allMine(this.owner);
            //@ assert this.next.allMine(this.next.owner);
            //@ assert this.next.next != null ==> this.next.next.allMine(this.next.next.owner);
            //@ assert this.owner == this.next.owner;
            //@ assert this.next.next != null ==> this.next.owner == this.next.next.owner;
            this.next = this.next.next;
            //@ assert this.owner != null;
            //@ assert this.next != null ==> this.next.allMine(this.next.owner);
            //@ assert this.allMine(this.owner) <==> (this.next != null ==> this.next.allMine(this.owner));
            //@ assert this.allMine(this.owner);
            if (this.next != null) {
                this.next.prev = this;
                //@ assert this.owner != null;
                //@ assert this.next != null ==> this.next.allMine(this.next.owner);
                //@ assert this.allMine(this.owner) <==> (this.next != null ==> this.next.allMine(this.owner));
                //@ assert this.allMine(this.owner);
            } else {
                //@ assert this.owner != null;
                //@ assert this.next != null ==> this.next.allMine(this.next.owner);
                //@ assert this.allMine(this.owner) <==> (this.next != null ==> this.next.allMine(this.owner));
                //@ assert this.allMine(this.owner);
            }
            //@ assert this.owner != null;
            //@ assert this.next != null ==> this.next.allMine(this.next.owner);
            //@ assert this.allMine(this.owner) <==> (this.next != null ==> this.next.allMine(this.owner));
            //@ assert this.allMine(this.owner);
        } else {
            this.next.remove(n-1);
            //@ assert this.owner != null && allMine(this.owner);
        }
        //@ assert this.owner == \old(this.owner);
        //@ assert this.owner != null && allMine(this.owner);
    }
}

public class ListDLL<T> extends Link<T> {
    
    //@ public invariant this.owner == this;
    //@ public invariant this.prev == null;
    
    /** Creates an empty list -- just an anchor node with null 'next' field */
    //@ public normal_behavior
    //@   ensures \result.values == seq.<TT>empty();
    //@   ensures \result.size == 0;
    //@   ensures \result.links == seq.<Link<TT>>empty();
    //@   ensures \result.next == null;
    //@   ensures \result.owner == \result;
    //@ pure
    public static <TT> ListDLL<TT> empty() {
        return new ListDLL<TT>();
    }
    
    //@ private normal_behavior
    //@   ensures this.next == null;
    //@   ensures this.prev == null;
    //@   ensures this.owner == this;
    //@ pure
    private ListDLL() {
        this.next = null;
        this.prev = null;
        //@ set this.owner = this;
    }
    
    /** Pushes a new value onto the front of the list */
    //@ public normal_behavior
    //@   assignable size, values, links;
    //@   ensures this.size == \old(size) + 1;
    //@   ensures this.values == \old(values).prepend(t);
    //@   ensures this.links == \old(links).prepend(this.next);
    //@   ensures \fresh(this.next);
    //@   ensures this.next.value == t;
    //@   ensures this.next.next == \old(this.next);
    public void push(T t) {
        // @   assume next != null ==> \invariant_for(next); // FIXME - why?
        //@ assert allMine(this);
        //@ assert this.next != null ==> this.next.allMine(this);
        // @   assert next != null ==> \invariant_for(next);
        // @   assert this != null ==> \invariant_for(this);
        //@ reachable;
        var v = new ListDLL.Node<T>(t, next, this);
        //@ reachable;
        //@ assert (v.owner == this && (v.next != null ==> v.next.allMine(v.owner)));
        //@ assert v.allMine(this);
        //@ reachable;
        if (next != null) next.prev = v;
        //@ reachable;
        //@ assert v.next == this.next;
        //@ assert (v.owner == this && (v.next != null ==> v.next.allMine(v.owner)));
        //@ assert v.allMine(this);
        //@ set v.owner = this;
        //@ assert (v.owner == this && (v.next != null ==> v.next.allMine(v.owner)));
        //@ assert v.allMine(this);
        //@ reachable;
        this.next = v;
        //@ assert this.next != null ==> this.next.allMine(owner);
        //@ assert this.allMine(this);
        //@ reachable;
    }
    
    /** Removes the first value from the list */
    //@ public normal_behavior
    //@   requires next != null;
    //@   old var oldvalues = this.values;
    //@   {|
    //@      requires this.next.next == null;
    //@      assignable size, values, links;
    //@    also
    //@      requires this.next.next != null;
    //@      assignable size, values, links, this.next.next.prev; // FIXME
    //@   |}
    //@   ensures this.size == \old(size) - 1;
    //@   ensures this.values == oldvalues.tail(1);
    //@   ensures this.links == \old(links).tail(1);
    //@   ensures next == \old(next.next);
    public void pop() {
        //@ assert this.owner == this && this.allMine(this.owner);
        //@ assert this.next.owner == this && this.next.allMine(this.owner);
        //@ assert this.next.next != null ==> this.next.next.allMine(this.next.owner);
        //@ ghost \bigint newsize = next.size;
        //@ ghost seq<T> oldnvalues = this.next.values;
        //@ ghost seq<Link<T>> oldnlinks = this.next.links;
        //@ assert next.size == this.size - 1;
        this.next = this.next.next;
        //@ assert this.next != null ==> this.next.allMine(this.next.owner);
        //@ assert this.values == oldnvalues;
        //@ assert this.links == oldnlinks;
        //@ assert this.allMine(this.owner) <==> (this.next != null ==> this.next.allMine(this.owner));
        //@ assert this.owner != null && allMine(this.owner);
        if (this.next != null) {
            //@ assert this.next != null ==> this.next.allMine(this.next.owner);
            //@ ghost var o = this.next.owner;
            this.next.prev = this;
            //@ assert this.values == oldnvalues;
            //@ assert this.links == oldnlinks;
            //@ assert o == this.next.owner;
            //@ assert this.next != null ==> this.owner == this.next.owner;
            // @ assert this.next != null ==> this.next.allMine(this.next.owner);
            //@ assert this.allMine(this.owner) <==> (this.owner == owner && (this.next != null ==> this.next.allMine(this.owner)));
            //@ assert this.allMine(this.owner);
        } else {
            //@ assert this.links == oldnlinks;
            //@ assert this.next != null ==> this.owner == this.next.owner;
            //@ assert this.next != null ==> this.next.allMine(this.next.owner);
            //@ assert this.allMine(this.owner) <==> (this.owner == owner && (this.next != null ==> this.next.allMine(this.owner)));
            //@ assert this.allMine(this.owner);
        }
        //@ assert this.size == newsize;
        //@ assert this.values == oldnvalues;
        //@ assert this.links == oldnlinks;
        // @ assert this.owner != null;
        //@ assert this.next != null ==> this.owner == this.next.owner;
        //@ assert this.next != null ==> this.next.allMine(this.next.owner);
        //@ assert this.allMine(this.owner) <==> (this.owner == owner && (this.next != null ==> this.next.allMine(this.owner)));
        //@ assert this.allMine(this.owner);
    }
    
    static class Node<T> extends Link<T> {

        /** Constructs a new link with given value and next field; for internal use only */
        //@ private normal_behavior
        //@   requires next != prev;
        //@   ensures this.next == next;
        //@   ensures this.prev == prev;
        //@   ensures this.value == value;
        //@ pure helper
        private Node(T value, /*@ helper nullable */ Node<T> next, /*@ helper nullable */ Link<T> prev) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }
        
        //@ spec_public
        private T value; //@ in values;
        
        /** Returns the value from this link */
        //@ public normal_behavior
        //@   reads value;
        //@   ensures \result == value;
        //@ pure
        public T value() {
            return value;
        }
    }
}

class Test {
    
    /** properties of an empty list */
    public static <Y> void testEmpty(Y y) {
        var in = ListDLL.<Y>empty();
        //@ assert in.size == 0;
        //@ assert in.values.size() == 0;
        //@ assert in.values == seq.<Y>empty();
    }

    /** pushing a value and then retrieving it */
    public static <Y> void testPushValue(ListDLL<Y> in, Y y, Y yy) {
        in.push(y);
        //@ assert in.size == \old(in.size) + 1;
        //@ assert in.values != \old(in.values);
        //@ assert in.value(0) == y;
        in.push(yy);
        //@ assert in.value(1) == y;
        //@ assert in.value(0) == yy;
    }
    
    /** pushing a value and then retrieving it */
    //@ requires in.size >= 2;
    public static <Y> void testPopValue(ListDLL<Y> in) {
        Y y = in.value(1);
        in.pop();
        Y yy = in.value(0);
        //@ assert y == yy;
    }
    
    /** pushing and popping leaves the list unchanged */
    public static <Y> void testPushPop(ListDLL<Y> in, Y y) {
        in.push(y);
        //@ assert in.size == \old(in.size) + 1;
        //@ assert in.values != \old(in.values);
        in.pop();
        //@ assert in.size == \old(in.size);
        //@ assert in.values == \old(in.values);
    }
    
    /** pushing and removing the zeroth element leaves the list unchanged */
    public static <Y> void testPushRemove(ListDLL<Y> in, Y y) {
        in.push(y);
        //@ assert in.size == \old(in.size) + 1;
        //@ assert in.values != \old(in.values);
        in.remove(0);
        //@ assert in.size == \old(in.size);
        //@ assert in.values == \old(in.values);
    }
    
    /** pushing a value on one list does not change the other */
    //@ requires in != other;
    //@ requires in.values == other.values;
    public static <Y> void testNI1(Y y, ListDLL<Y> in, ListDLL<Y> other) {
        in.push(y);
        //@ assert in.values.tail(1) == other.values;
    }
    
    /** popping a value from one list does not change the other */
    //@ requires in != other;
    //@ requires in.values == other.values;
    //@ requires in.size > 0;
    public static <Y> void testNI2(ListDLL<Y> in, ListDLL<Y> other) {
        //@ assert in.size == other.size;
        //@ ghost seq<Y> oldvalues = in.values;
        in.pop();
        //@ assert oldvalues.tail(1) == in.values;
        //@ assert in.values == other.values.tail(1);
    }
    
    /** removing a value (other than the 0th) from one list might change the other,
     * except when the owner invariant is used */
    //@ requires in != other;
    //@ requires in.values == other.values;
    //@ requires in.size > 1;
    public static <Y> void testNI3(ListDLL<Y> in, ListDLL<Y> other) {
        //@ ghost seq<Y> oldvalues = in.values;
        in.remove(1);
        //@ assert oldvalues.size - 1 == in.size;
        //@ assert other.values.size - 1 == in.size;   // Should not be provable without the owner invariant
    }
    
    /** two different lists may have the same first element, except when the owner invariant is used */
    //@ requires in != other;
    //@ requires in.size > 0;
    //@ requires other.size > 0;
    public static <Y> void testNI4(ListDLL<Y> in, ListDLL<Y> other) {
        //@ assert in.next.owner == in;
        //@ assert other.next.owner == other;
        //@ assert in.next != other.next;    // Should not be provable without the owner invariant
    }
    
    /** using rep-exposure to change a value still does not change the other list if the owner invariant is used */
    //@ requires in != other;
    //@ requires in.values == other.values;
    //@ requires in.size > 1;
    /*@ model public static <Y> void testNI5(ListDLL<Y> in, ListDLL<Y> other, Y y) {
        ghost \bigint n = in.size;
        assume in.next.value != y;
        assert in.values.size() == other.values.size();
        assert in.size == other.size;
        assert in.size == n;
        in.next().value = y;
        assert in.size == n;
        assert in.size == other.size;
        assert in.values != other.values;    // Should not be provable without the owner invariant
        assert in.values.tail(1) == other.values.tail(1);
    }
    @*/
}
