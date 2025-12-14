package com.example.mylibrary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 本地单元测试，测试 Book 实体类的逻辑。
 */
public class ExampleUnitTest {
    @Test
    public void book_statusText_isCorrect() {
        // 测试 status = 0 (想看)
        Book bookTodo = new Book(1, "Test Title", "Author", 0f, "", 0, "");
        assertEquals("想看", bookTodo.getStatusText());

        // 测试 status = 1 (阅读中)
        Book bookReading = new Book(1, "Test Title", "Author", 0f, "", 1, "");
        assertEquals("阅读中", bookReading.getStatusText());

        // 测试 status = 2 (已读)
        Book bookRead = new Book(1, "Test Title", "Author", 0f, "", 2, "");
        assertEquals("已读", bookRead.getStatusText());
    }

    @Test
    public void book_properties_areCorrect() {
        // 测试构造函数赋值是否正确
        Book book = new Book(100, "Java编程", "张三", 4.5f, "uri", 2, "path/to/file");
        assertEquals(100, book.getId());
        assertEquals("Java编程", book.getTitle());
        assertEquals(4.5f, book.getRating(), 0.01);
    }
}