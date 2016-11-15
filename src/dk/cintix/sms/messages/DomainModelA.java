/*
 */
package dk.cintix.sms.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author migo
 */
public class DomainModelA extends Message {

    private String title = "My Awsome Title";
    private int id = 6;
    private List<Movie> movies;

    public DomainModelA() {

        Movie a = new Movie();
        Movie b = new Movie();

        a.setId(1);
        a.setTitle("Title A");

        b.setId(2);
        b.setTitle("Title B");

        movies = new ArrayList<>();
        movies.add(a);
        movies.add(b);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public class Movie implements Serializable{

        private String title;
        private int id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Movie{" + "title=" + title + ", id=" + id + '}';
        }

    }

    @Override
    public String toString() {
        return "DomainModelA {" + "title=" + title + ", id=" + id + ", movies=" + movies + '}';
    }

}
