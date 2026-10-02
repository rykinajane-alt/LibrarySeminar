
package ru.library.libraryseminar.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.library.libraryseminar.entity.Book;
import ru.library.libraryseminar.service.BookService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Главная страница: список, поиск и фильтрация
    @GetMapping
    public String getAllBooks(
            @RequestParam(required = false) String publishingHouse,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Model model) {

        // Проверяем правильность диапазона дат
        boolean invalidDateRange =
                startDate != null
                        && endDate != null
                        && startDate.isAfter(endDate);

        if (invalidDateRange) {
            model.addAttribute("books", List.of());
            model.addAttribute(
                    "dateError",
                    "Начальная дата не может быть позже конечной"
            );
        } else {
            model.addAttribute(
                    "books",
                    bookService.filterBooks(
                            publishingHouse,
                            startDate,
                            endDate
                    )
            );
        }

        // Сохраняем введённые значения фильтров
        model.addAttribute("publishingHouse", publishingHouse);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Данные гистограммы за последние 30 дней
        Map<LocalDate, Long> histogram =
                bookService.getIssueHistogram30Days();

        model.addAttribute("histogram", histogram);

        // Максимальное значение для масштабирования столбцов
        long maxCount = histogram.values()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        model.addAttribute("maxCount", maxCount);

        return "books";
    }

    // Форма добавления книги
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("pageTitle", "Добавление книги");

        return "book-form";
    }

    // Сохранение новой или отредактированной книги
    @PostMapping("/save")
    public String saveBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    book.getId() == null
                            ? "Добавление книги"
                            : "Редактирование книги"
            );

            return "book-form";
        }

        bookService.saveBook(book);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Книга успешно сохранена"
        );

        return "redirect:/books";
    }

    // Форма редактирования книги
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("book", bookService.getBookById(id));
        model.addAttribute("pageTitle", "Редактирование книги");

        return "book-form";
    }

    // Удаление книги
    @PostMapping("/delete/{id}")
    public String deleteBook(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        bookService.deleteBook(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Книга успешно удалена"
        );

        return "redirect:/books";
    }
}
