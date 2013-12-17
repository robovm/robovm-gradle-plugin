/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jtakakura.gradle.plugins.robovm;

import com.github.jtakakura.gradle.tasks.robovm.CreateIPATask;
import com.github.jtakakura.gradle.tasks.robovm.IOSDeviceTask;
import com.github.jtakakura.gradle.tasks.robovm.IPadSimulatorTask;
import com.github.jtakakura.gradle.tasks.robovm.IPhoneSimulatorTask;
import groovy.lang.Closure;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Junji Takakura
 */
public class RoboVMPluginTest {

    private Project project;

    public RoboVMPluginTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build();
        project.apply(new HashMap<String, Object>() {
            {
                put("plugin", "robovm");
            }
        });
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetTaskByNameLaunchIPhoneSimulator() {
        Task task = project.getTasks().getByName("launchIPhoneSimulator");

        assertTrue(task instanceof IPhoneSimulatorTask);
        assertEquals(task.getGroup(), null);
        assertEquals(task.getDescription(), null);
    }

    @Test
    public void testGetTaskByNameLaunchIPadSimulator() {
        Task task = project.getTasks().getByName("launchIPadSimulator");

        assertTrue(task instanceof IPadSimulatorTask);
        assertEquals(task.getGroup(), null);
        assertEquals(task.getDescription(), null);
    }

    @Test
    public void testGetTaskByNameLaunchIOSDevice() {
        Task task = project.getTasks().getByName("launchIOSDevice");

        assertTrue(task instanceof IOSDeviceTask);
        assertEquals(task.getGroup(), null);
        assertEquals(task.getDescription(), null);
    }

    @Test
    public void testGetTaskByNameCreateIPA() {
        Task task = project.getTasks().getByName("createIPA");

        assertTrue(task instanceof CreateIPATask);
        assertEquals(task.getGroup(), null);
        assertEquals(task.getDescription(), null);
    }
}
