package com.example.ju_os;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class AppTest {
    @Test public void testApp() { assertNotNull(new App()); }
    @Test public void testVersion() { assertTrue("0.0.1".matches("\d+\.\d+\.\d+")); }
}
