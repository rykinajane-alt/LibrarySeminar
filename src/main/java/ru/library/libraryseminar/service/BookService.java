
package ru.library.libraryseminar.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.library.libraryseminar.entity.Book;
import ru.library.libraryseminar.repository.BookRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Получение всех книг с сортировкой по дате выдачи и названию
    public List<Book> getAllBooks() {
        return bookRepository.findAll(
                Sort.by(
                        Sort.Order.asc("issueDate"),
                        Sort.Order.asc("title")
                )
        );
    }

    // Получение книги по ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Книга не найдена"));
    }

    // Добавление или редактирование книги
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // Удаление книги
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // Поиск по издательству
    public List<Book> searchByPublishingHouse(String publishingHouse) {
        return bookRepository
                .findByPublishingHouseContainingIgnoreCase(publishingHouse);
    }

    // Фильтрация по издательству и диапазону дат выдачи
    public List<Book> filterBooks(
            String publishingHouse,
            LocalDate startDate,
            LocalDate endDate) {

        return getAllBooks().stream()
                .filter(book ->
                        publishingHouse == null
                                || publishingHouse.isBlank()
                                || (book.getPublishingHouse() != null
                                && book.getPublishingHouse()
                                .toLowerCase()
                                .contains(publishingHouse.toLowerCase()))
                )
                .filter(book ->
                        startDate == null
                                || (book.getIssueDate() != null
                                && !book.getIssueDate().isBefore(startDate))
                )
                .filter(book ->
                        endDate == null
                                || (book.getIssueDate() != null
                                && !book.getIssueDate().isAfter(endDate))
                )
                .toList();
    }

    // Подсчёт выдач за последние 30 дней, включая сегодняшний день
    public Map<LocalDate, Long> getIssueHistogram30Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);

        Map<LocalDate, Long> dailyCounts = bookRepository.findAll()
                .stream()
                .filter(book -> book.getIssueDate() != null)
                .filter(book ->
                        !book.getIssueDate().isBefore(startDate)
                                && !book.getIssueDate().isAfter(today)
                )
                .collect(Collectors.groupingBy(
                        Book::getIssueDate,
                        Collectors.counting()
                ));

        // Заполняем все 30 дней, в том числе дни без выдач
        Map<LocalDate, Long> histogram = new LinkedHashMap<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            histogram.put(date, dailyCounts.getOrDefault(date, 0L));
        }

        return histogram;
    }
}
