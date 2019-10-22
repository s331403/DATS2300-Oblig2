package no.oslomet.cs.algdat;


////////////////// class DobbeltLenketListe //////////////////////////////


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;



public class DobbeltLenketListe<T> implements Liste<T> {

    /**
     * Node class
     * @param <T>
     */
    private static final class Node<T> {
        private T verdi;                   // nodens verdi
        private Node<T> forrige, neste;    // pekere

        private Node(T verdi, Node<T> forrige, Node<T> neste) {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        private Node(T verdi) {
            this(verdi, null, null);
        }
    }

    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;         // antall endringer i listen

    public DobbeltLenketListe() {
        hode = hale = null;
        antall = 0;
        endringer = 0;
    }

    public DobbeltLenketListe(T[] a) {
        this();
        /**
         * Null element check
         */
        if(a == null) {
            throw new NullPointerException("Tabellen a er null!");
        }

        /**
         * Empty list check
         */
        if(a.length == 0) {
            return;
        }

        /**
         * Find the first element that`s different from null
         */
        int scan = 0;
        while(a[scan] == null && scan < a.length-1) {
            scan++;
        }

        /**
         * If there is a element not equal to null initiate
         * head and tail else return default constructor
         */
        if( scan < a.length && a[scan] != null) {
            hode = hale = new Node<>(a[scan], null, null);
            antall++;
        }
        else return;

        Node<T> current = hode;
        /**
         * Start on the next element and scan the array for not null elements
         */
        for(scan = scan+1; scan < a.length; scan++) {
            if(a[scan] != null) {
                antall++;
                current.neste = new Node<>(a[scan]);
                current.neste.forrige = current;
                current = current.neste;
            }
        }
        hale = current;
        hale.neste = null;
    }

    public Liste<T> subliste(int fra, int til){
        fratilKontroll(fra, til);
        Node<T> current = finnNode(fra);
        Liste<T> liste = new DobbeltLenketListe<>();
        if(fra == til) return liste;
        liste.leggInn(current.verdi);
        for(int scan=0; scan<til-fra-1; scan++) {
            liste.leggInn(current.neste.verdi);
            current = current.neste;
        }
        return liste;
    }

    private void fratilKontroll(int fra, int til) {
        if(fra < 0 || til > antall)
            throw new IndexOutOfBoundsException("Indeksene er utenfor arrayet");
        if(fra > til)
            throw new IllegalArgumentException("Fra er større en til!");
    }

    @Override
    public int antall() {
       return antall;
    }

    @Override
    public boolean tom() {
        return antall == 0 ? true : false;
    }

    @Override
    public boolean leggInn(T verdi) {
        Node<T> current = new Node<>(Objects.requireNonNull(verdi, "Kan ikke være null!"));
        if(antall == 0) {
            hode = hale = current;
        }
        else{
            hale.neste = current;
            hale.neste.forrige = hale;
            hale = hale.neste;
        }
        antall++;
        endringer++;
        return true;
    }

    private Node<T> finnNode(int index) {
        Node<T> current;
        if(index < antall/2) {
            current = hode;
            for(int scan=0; scan<index; scan++) {
                current = current.neste;
            }
        }
        else {
            current = hale;
            for(int scan=0; scan < antall-index-1; scan++) {
                current = current.forrige;
            }
        }
        return current;
    }

    @Override
    public void leggInn(int indeks, T verdi) {
        if(indeks < 0 || antall < indeks)
            throw new IndexOutOfBoundsException();
        if(verdi == null)
            throw new NullPointerException();
        // 1) listen er tom
        Node<T> current = new Node<>(verdi);
        if(antall == 0) {
            hode = hale = current;
        }
        // 2) verdien skal legges først
        else if(indeks == 0) {
            current.neste = hode;
            hode.forrige = current;
            hode = current;
        }
        // 3) verdien skal legges bakerst
        else if(indeks == antall) {
            current.forrige = hale;
            hale.neste = current;
            hale = current;
        }
        // 4) verdien skal legges mellom to andre verdier
        else {
            Node<T> node = finnNode(indeks);
            current.forrige = node.forrige;
            current.forrige.neste = current;
            node.forrige = current;
            current.neste = node;
        }
        antall++;
        endringer++;
    }

    @Override
    public boolean inneholder(T verdi) {
        return indeksTil(verdi) != -1 ? true : false;
    }

    @Override
    public T hent(int indeks) {
        this.indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }

    @Override
    public int indeksTil(T verdi) {
        Node<T> current = hode;
        int index = 0;
        while(current != null) {
            if(current.verdi.equals(verdi))
                return index;
            else index++;
            current = current.neste;
        }
        return -1;
    }

    @Override
    public T oppdater(int indeks, T nyverdi) {
        this.indeksKontroll(indeks, false);
        if(nyverdi == null) throw new NullPointerException("Kan ikke legge til null verdier");
        Node<T> current = finnNode(indeks);
        T oldValue = current.verdi;
        current.verdi = nyverdi;
        endringer++;
        return oldValue;
    }

    @Override
    public boolean fjern(T verdi) {
        if(verdi == null || antall == 0)
            return false;
        Node<T> current = hode;
        if(hode.verdi.equals(verdi)) {
            if(antall == 1) {
                hode = hale = null;
            }
            else {
                hode.neste.forrige = null;
                hode = hode.neste;
            }
            antall--;
            endringer++;
            return true;
        }
        for(int scan=0; scan<antall; scan++) {
            if(current.verdi.equals(verdi)) {
                if(scan == antall-1) {
                    hale.forrige.neste = null;
                    hale = hale.forrige;
                }
                else {
                    current.forrige.neste = current.neste;
                    current.neste.forrige = current.forrige;
                }
                antall--;
                endringer++;
                return true;
            }
            else {
                current = current.neste;
            }
        }
        return false;
    }

    @Override
    public T fjern(int indeks) {
        if(antall == 0 || indeks < 0 || antall <= indeks) {
            System.out.println(antall + " " + indeks);
            throw new IndexOutOfBoundsException();
        }
        if(antall == 1) {
            antall--;
            endringer++;
            T verdi = hode.verdi;
            hode = hale = null;
            return verdi;
        }
        T verdi;
        antall--;
        endringer++;
        if(indeks == 0){
            verdi = hode.verdi;
            hode.neste.forrige = null;
            hode = hode.neste;
        }
        else if(indeks == antall) {
            verdi = hale.verdi;
            hale.forrige.neste = null;
            hale = hale.forrige;
        }
        else {
            Node<T> node = hode;
            for(int scan=0; scan<indeks; scan++) {
                node = node.neste;
            }
            verdi = node.verdi;
            node.forrige.neste = node.neste;
            node.neste.forrige = node.forrige;
        }
        if(antall == 0) {
            hode = hale = null;
        }
        return verdi;
    }

    @Override
    public void nullstill() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        Node<T> current;
        if(hode != null) {
            current = hode;
            builder.append(hode.verdi);
            while(current.neste != null) {
                builder.append(", ");
                current = current.neste;
                builder.append(current.verdi);
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public String omvendtString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        Node<T> current;
        if(hale != null) {
            current = hale;
            builder.append(hale.verdi);
            while(current.forrige != null) {
                builder.append(", ");
                current = current.forrige;
                builder.append(current.verdi);
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Iterator<T> iterator() {
        throw new NotImplementedException();
    }

    public Iterator<T> iterator(int indeks) {
        throw new NotImplementedException();
    }

    private class DobbeltLenketListeIterator implements Iterator<T>
    {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;

        private DobbeltLenketListeIterator(){
            throw new NotImplementedException();
        }

        private DobbeltLenketListeIterator(int indeks){
            throw new NotImplementedException();
        }

        @Override
        public boolean hasNext(){
            throw new NotImplementedException();
        }

        @Override
        public T next(){
            throw new NotImplementedException();
        }

        @Override
        public void remove(){
        }

    } // class DobbeltLenketListeIterator

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
        throw new NotImplementedException();
    }

} // class DobbeltLenketListe


