package com.github.bfalmeida.photosync.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyncConfigPanelTest {

    private SyncConfigPanel panel;

    @BeforeEach
    void setUp() {
        panel = new SyncConfigPanel();
    }

    @Test
    void testDefaultValues() {
        assertEquals("", panel.getSourcePath());
        assertEquals("", panel.getDestPath());
        assertEquals("undated", panel.getUndatedFolder());
        assertFalse(panel.isClearState());
        assertFalse(panel.isSkipUndated());
    }

    @Test
    void testSetSourcePath_UpdatesField() {
        panel.setSourcePath("/test/source");
        assertEquals("/test/source", panel.getSourcePath());
    }

    @Test
    void testSetDestPath_UpdatesField() {
        panel.setDestPath("/test/dest");
        assertEquals("/test/dest", panel.getDestPath());
    }

    @Test
    void testSetSourcePath_NullHandled() {
        panel.setSourcePath(null);
        assertEquals("", panel.getSourcePath());
    }

    @Test
    void testSetSourcePath_EmptyHandled() {
        panel.setSourcePath("");
        assertEquals("", panel.getSourcePath());
    }

    @Test
    void testUndatedFolder_SetViaConstructor() {
        // Undated folder is initialized in constructor with default "undated"
        assertEquals("undated", panel.getUndatedFolder());
    }

    @Test
    void testPanelLayoutNotNull() {
        assertNotNull(panel.getLayout());
        assertNotNull(panel.getBackground());
    }

    @Test
    void testMultipleSetOperations() {
        panel.setSourcePath("/path/one");
        panel.setDestPath("/path/two");
        panel.setSourcePath("/path/three");
        
        assertEquals("/path/three", panel.getSourcePath());
        assertEquals("/path/two", panel.getDestPath());
    }
}