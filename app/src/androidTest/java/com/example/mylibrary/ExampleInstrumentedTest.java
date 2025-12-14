package com.example.mylibrary;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * 仪器化测试，运行在 Android 设备上，测试数据库功能。
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.mylibrary", appContext.getPackageName());
    }

    @Test
    public void dbHelper_addAndRetrieveBook() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        BookDbHelper dbHelper = new BookDbHelper(appContext);

        // 1. 清理数据（可选，防止干扰）
        // dbHelper.getWritableDatabase().execSQL("DELETE FROM " + BookContract.BookEntry.TABLE_NAME);

        // 2. 添加一本书
        long userId = 12345;
        long bookId = dbHelper.addBook(userId, "Test DB Book", "Tester", 5.0f, "", 0, "");
        assertTrue("Book ID should be valid", bookId != -1);

        // 3. 读取这本书
        Book book = dbHelper.getBook(bookId);
        assertNotNull("Book should acturally exist", book);
        assertEquals("Title should match", "Test DB Book", book.getTitle());
        assertEquals("Author should match", "Tester", book.getAuthor());

        // 4. 清理测试数据
        dbHelper.deleteBook(bookId);
        Book deletedBook = dbHelper.getBook(bookId);
        assertNull("Book should be null after delete", deletedBook);
    }
}