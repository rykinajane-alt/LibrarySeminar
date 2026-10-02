
package ru.library.libraryseminar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.library.libraryseminar.entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByPublishingHouseContainingIgnoreCase(String publishingHouse);

    List<Book> findAllByOrderByIssueDateAscTitleAsc();
}
